#!/usr/bin/env python3

"""
Usage:
./map_merge.py <target_file> <target_width> <target_height> {<source_file> <source_x> <source_y> <source_w> <source_h> <dest_x> <dest_y>}...

later maps overwrite former; layers with same names are merged; tileset of the first input is used
actions set-tile, laser-dev-toggle and laser-dev-rotate have their coordinates transformed
layer order is preserved, but anything in a higher map's layer is merged into a lower map's layer with the same name
"""

from xml.etree import ElementTree
from sys import argv
from base64 import b64encode, b64decode
from gzip import compress, decompress
from struct import unpack

properties = [] #[(name, value)]
layers = [] #[(name, properties:list, data:bytearray)]
ogroups = [] #[(name, objects:list)]
#object: [(name, type, x, y, gid, props)]
tileset = None

new_map_width = int(argv[2])
new_map_height = int(argv[3])
new_map_owidth = new_map_width*24
new_map_oheight = new_map_height*24

def get_layer(n):
	for l in layers:
		if l[0] == n:
			return l
	l = (n, [], bytearray(new_map_height*new_map_width*4))
	layers.append(l)
	return l

def get_ogroup(n):
	for og in ogroups:
		if og[0] == n:
			return og
	og = (n, [])
	ogroups.append(og)
	return og


for i in range(4, len(argv), 7):
	et = ElementTree.parse(argv[i])
	src_x = int(argv[i+1])
	src_y = int(argv[i+2])
	src_w = int(argv[i+3])
	src_h = int(argv[i+4])
	offset_x = int(argv[i+5])
	offset_y = int(argv[i+6])
	src_ox = src_x*24;
	src_oy = src_y*24;
	src_ow = src_w*24;
	src_oh = src_h*24;
	offset_ox = offset_x*24
	offset_oy = offset_y*24
	for e in et.getroot(): #<map>
		if e.tag == "properties":
			for prop in e:
				properties.append((prop.get('name'), prop.get('value')))
		elif e.tag == "tileset":
			if tileset is None:
				tileset = e
		elif e.tag == "layer":
			layer = get_layer(e.get('name'))
			layer_w = int(e.get('width'))
			layer_h = int(e.get('height'))
			for layer_child in e:
				if layer_child.tag == "properties":
					for layer_prop in layer_child:
						layer[1].append((layer_prop.get('name'), layer_prop.get('value')))
				elif layer_child.tag == "data":
					data = decompress(b64decode(layer_child.text))+b'\x00\x00\x00\x00'
					for y in range(src_y, src_y+src_h):
						for x in range(src_x, src_x+src_w):
							old_index = 4*((layer_w*y)+x)
							#print(x, y, data[old_index:old_index+4], x-src_x+offset_x, y-src_y+offset_y)
							new_index = 4*((new_map_width*(y-src_y+offset_y))+(x-src_x+offset_x))
							if unpack('<I', data[old_index:old_index+4])[0] != 0:
								layer[2][new_index:new_index+4] = data[old_index:old_index+4]
		elif e.tag == "objectgroup":
			og = get_ogroup(e.get('name'))
			for obj in e:
				oname = obj.get('name', None)
				otype = obj.get('type', None)
				ox = int(obj.get('x')) + offset_ox
				oy = int(obj.get('y')) + offset_oy
				#if ox < src_ox or ox >= src_ox+src_ow or oy < src_oy or oy > src_oy+src_oh:
				#	continue
				obj_n = (oname, otype, ox-src_ox, oy-src_oy, obj.get('gid'), [])
				og[1].append(obj_n)
				print(obj_n)
				for obj_child in obj:
					if obj_child.tag == 'properties':
						for obj_prop in obj_child:
							prop_name = obj_prop.get('name')
							prop_val = obj_prop.get('value')
							if prop_name in ('enable', 'disable', 'lock', 'unlock'):
								actions = prop_val.split(',')
								for i, a in enumerate(actions):
									parts = a.split(':')
									if parts[0] in ('set-tile', 'door-open', 'door-close'):
										parts[1] = str(int(parts[1]) - src_x + offset_x)
										parts[2] = str(int(parts[2]) - src_y + offset_y)
									elif parts[0] in ('laser-dev-toggle', 'laser-dev-rotate'):
										parts[2] = str(int(parts[2]) - src_x + offset_x)
										parts[3] = str(int(parts[3]) - src_y + offset_y)
									elif parts[0] in ('device-drop', 'zombie-spawn', 'zombie-spawn-x'):
										parts[2] = str(float(parts[2]) - src_x + offset_x)
										parts[3] = str(float(parts[3]) - src_y + offset_y)
									elif parts[0] in ('keycard-drop', 'health-drop'):
										parts[3] = str(float(parts[3]) - src_x + offset_x)
										parts[4] = str(float(parts[4]) - src_y + offset_y)
									actions[i] = ":".join(parts)
								prop_val = ",".join(actions)
							obj_n[-1].append((prop_name, prop_val))

new_map = ElementTree.Element('map', {'version': "1.0", 'orientation': 'orthogonal', 'width': str(new_map_width), 'height': str(new_map_height), 'tilewidth': "24", "tileheight": "24"})
new_map.append(tileset)

if properties:
	e_props = ElementTree.Element('properties')
	for name, value in properties:
		print("map property: "+name+"="+value)
		e_props.append(ElementTree.Element('property', {'name': name, 'value': value}))
	new_map.append(e_props)

for name, props, data in layers:
	print("layer: "+name)
	e_layer = ElementTree.Element('layer', {'name': name, 'width': str(new_map_width), 'height': str(new_map_height)})
	if props:
		e_props = ElementTree.Element('properties')
		for name, value in props:
			e_props.append(ElementTree.Element('property', {'name': name, 'value': value}))
		e_layer.append(e_props)
	e_data = ElementTree.Element('data', {'encoding': 'base64', 'compression': 'gzip'})
	data = data[0:new_map_width*new_map_height*4]
	e_data.text = str(b64encode(compress(data)),'utf-8')
	e_layer.append(e_data)
	new_map.append(e_layer)

for name, objects in ogroups:
	e_ogroup = ElementTree.Element('objectgroup', {'name': name, 'width': str(new_map_width), 'height': str(new_map_height)})
	for name, typ, x, y, gid, props in objects:
		print("object: "+str(typ)+"@("+str(x)+";"+str(y)+")")
		e_obj = ElementTree.Element('object', {'x': str(x), 'y': str(y), 'width': str(24), 'height': str(24)})
		if name is not None:
			e_obj.set('name', name)
		if typ is not None:
			e_obj.set('type', typ)
		if gid is not None:
			e_obj.set('gid', gid)
		if props:
			e_props = ElementTree.Element('properties')
			for name, value in props:
				e_props.append(ElementTree.Element('property', {'name': name, 'value': value}))
			e_obj.append(e_props)
		e_ogroup.append(e_obj)
	new_map.append(e_ogroup)

from xml.dom import minidom

with open(argv[1], 'w') as f:
	f.write(minidom.parseString(ElementTree.tostring(new_map, 'utf-8')).toprettyxml(indent="\t"))
