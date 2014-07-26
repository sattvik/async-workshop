Get going with core.async
=========================

This repository holds the materials for 'Get going with core.async', a workshop being presented at Lambda Jam Chicago 2014 and Strange Loop 2014.

Starting the tutorial
---------------------

Once you have cloned the repository, you should be by default on the a-minimal-client branch.  To get things fully started, just follow these steps:

1. In a terminal window, run lein repl.
2. Once the REPL has finished starting, start the server using (user/start).
3. You can now browse the tutorial website from [http://localhost:9000](http://localhost:9000).
4. You will probably want to run lein cljsbuild auto in separate REPL window.

If you are an experienced ClojureScript developer and want to connect your editor to a browser REPL, you can use the REPL environment in @user/repl-env once you have started the server the first time.


A note for OS X users
---------------------

It appears that this tutorial requires Java 7 or newer, but the default is Java 6.  Please update your Java.
