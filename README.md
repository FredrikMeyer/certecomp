# certecomp

[![codecov](https://codecov.io/gh/FredrikMeyer/certecomp/branch/main/graph/badge.svg?token=KFQauFSPcQ)](https://codecov.io/gh/FredrikMeyer/certecomp)

At the moment I'm just trying to build an exercise logger with a Clojure backend.

> _certe computa_ is Latin for "be sure to calculate"



## Usage

Run backend on `localhost:3000`. Go to `backend` and run `lein run`.

Frontend at http://127.0.0.1:5173/. Run by `yarn dev`.

## TODO

 - CI build + test


### Look at

https://github.com/magnars/parens-of-the-dead-s2/blob/main/src/undead/system.clj

clojure jdbc
https://clojure-doc.org/articles/ecosystem/java_jdbc/using_sql/

https://cljdoc.org/d/com.github.seancorfield/next.jdbc/1.3.847/doc/getting-started

Possibly next.jdbc instead of java.jdbc.

https://cljdoc.org/d/buddy/buddy-auth/3.0.1/doc/user-guide

Auth-bibliotek for ring

https://grison.me/2020/04/23/webauthn-with-clojure/

https://cljdoc.org/d/metosin/reitit/0.5.18/doc/misc/faq
alternativ til compojure

https://cljdoc.org/d/com.github.seancorfield/honeysql/2.4.980/doc/readme
for nicer sql

https://otee.dev/2022/01/25/clojure-backend-using-ring-jetty-compojure.html
blogpost

https://practical.li/clojure-web-services/app-servers/atom-based-restart.html
mye bra


https://www.kodemaker.no/blogg/2019-10-cljss/

CSS-rammerverk
https://bulma.io/documentation/elements/button/

https://github.com/funcool/buddy

https://github.com/stuartsierra/component
Kanskje denne ?

https://github.com/aliaksandr-s/prototyping-with-clojure/blob/master/tutorial/chapter-05/05-Registration%20and%20authentication.md

logging
https://github.com/ptaoussanis/timbre


https://yobriefca.se/blog/2014/04/29/managing-environment-variables-in-clojure/

## License


Copyright © 2023 Fredrik Meyer

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
