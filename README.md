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


Legal stuff
-----------

Copyright © 2014 Daniel Solano Gómez
All rights reserved.

Except as otherwise noted:

* All code is licensed under the [Eclipse Public License 1.0][EPL-1.0]
* All documentation is licensed under a [Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License][CC-BY-SA-4.0]

  [EPL-1.0]: http://www.eclipse.org/legal/epl-v10.html
  [CC-BY-SA-4.0]: http://creativecommons.org/licenses/by-nc-sa/4.0/

### Included works

* Roboto font by Christian Robertson and licensed under the [Apache License 2.0][AL-2.0]
* Inconsolata font by Raph Levien and licensed under the [Open Font License][OFL]
* Polymer is licensed under the [Polymer BSD license][PolymerBSD]
* React is licensed under the [Apache License 2.0][AL-2.0]

  [AL-2.0]: http://www.apache.org/licenses/LICENSE-2.0.html
  [OFL]: http://scripts.sil.org/cms/scripts/page.php?site_id=nrsi&id=OFL
  [PolymerBSD]: http://polymer.github.io/LICENSE.txt
