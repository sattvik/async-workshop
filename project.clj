(defproject io.clojure/async-workshop "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2268"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [com.cemerick/friend "0.2.1" :exclusions [org.clojure/core.cache]]
                 [compojure "1.1.8"]
                 [enlive "1.1.5"]
                 [http-kit "2.1.16"]
                 [javax.servlet/servlet-api "2.5"]
                 [om "0.6.4"]
                 [ring/ring-core "1.3.0"]]
  :source-paths ["src/clj" "src/cljs"]
  :resource-paths ["resources"]
  :main async-workshop.server
  :hooks [leiningen.cljsbuild]
  :profiles {:dev {:source-paths ["devel/clj"]
                   :dependencies [[ring/ring-devel "1.3.0"]]
                   :plugins [[com.cemerick/austin "0.1.4"]
                             [lein-cljsbuild "1.0.3"]]
                   :cljsbuild {:builds [{:id "devel"
                                         :source-paths ["src/cljs"]
                                         :compiler {:output-to "target/classes/public/js/chatDriver.js"
                                                    :output-dir "target/classes/public/js"
                                                    :optimizations :none
                                                    :source-map true}}]}}
             :uberjar {:aot :all}})
