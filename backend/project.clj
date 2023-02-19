(defproject certecomp "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [ring/ring-core "1.8.2"]
                 [ring/ring-jetty-adapter "1.8.2"]
                 [ring/ring-mock "0.4.0"]
                 [metosin/reitit "0.5.18"]
                 [org.clojure/java.jdbc "0.7.8"]
                 [org.xerial/sqlite-jdbc "3.23.1"]]
  :profiles {:dev {:dependencies
                   [[org.clojure/clojurescript "1.11.60"]
                    [com.bhauman/figwheel-main "0.2.18"]]}}
  :repl-options {:init-ns certecomp.core}
  :source-paths ["src"]
  :resource-paths ["target" "resources"])
