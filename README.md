Dichotomy
=============

Entry to [Ludum Dare 30](http://www.ludumdare.com/compo/ludum-dare-30/?action=preview&uid=41722), for the theme "Connected Worlds".

> "Those who say technology is the great enabler forget it is a means, not an end." 

In Dichotomy, you solve various puzzles by swapping between two alternate worlds and redirecting lasers. In one of the worlds, monsters will try and attack you. We've packaged a readme containing the controls. 

###Credits

Coded by Tom and Toby, with IntelliJ Idea, Notepad++ and Sublime Text, in Java, using LWJGL and Slick2d, with some Python support scripts. 

Tile and sprite graphics by Lennart and Felix (with contributions from Tom and Toby), made in GIMP, Paint.NET and Pinta. 

Level design by Felix and Toby, using the Tiled map editor. 

Sound effects by Tom, made with sfxr. 

Story by Tom and Toby, using pure imagination.

Background music is "Constance" by Kevin MacLeod (incompetech.com), Licensed under [Creative Commons Attribution 3.0 ](http://creativecommons.org/licenses/by/3.0/)

### Nolstalgic Notations

Compile with `mvn package`. Start with `java -Djava.library.path=target/natives -jar target/dichotomy-1.0-SNAPSHOT.jar`.

If maven complains of a missing `javaws.jar` (while looking for `jnlp-api` for Slick2d), copy and rename IcedTea's `netx.jar` to satisfy it. (I phrased that to be reasonably googleable, and I hope the next person will find this before all the useless things I found.)
