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
                 [metosin/reitit-swagger "0.6.0"]
                 [metosin/reitit-swagger-ui "0.6.0"]
                 [integrant/integrant "0.8.0"]
                 [com.taoensso/timbre "6.1.0"]
                 [com.github.seancorfield/next.jdbc "1.3.847"]
                 [org.xerial/sqlite-jdbc "3.23.1"]
                 [com.github.seancorfield/honeysql "2.4.1026"]]
  :profiles {:dev {:dependencies
                   [[org.clojure/clojurescript "1.11.60"]
                    [com.bhauman/figwheel-main "0.2.18"]]}}
  :repl-options {:init-ns certecomp.core}
  :plugins [[lein-cloverage "1.2.4"]]
  :main certecomp.core
  :source-paths ["src"]
  :resource-paths ["target" "resources"])
