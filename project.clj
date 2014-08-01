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
  :plugins [[lein-cljsbuild "1.0.3"]]
  :source-paths ["src/clj" "src/chat-demo" "src/channel-demo"]
  :resource-paths ["resources"]
  :main async-workshop.server
  :hooks [leiningen.cljsbuild]
  :profiles {:dev {:source-paths ["devel/clj" "dev/cljs"]
                   :dependencies [[ring/ring-devel "1.3.0"]]
                   :plugins [[com.cemerick/austin "0.1.4"]]
                   :cljsbuild {:builds [{:id "chat-demo"
                                         :source-paths ["src/chat-demo"]
                                         :compiler {:output-to "target/classes/public/js/chat-demo.js"
                                                    :output-dir "target/classes/public/js/chat-demo"
                                                    :optimizations :none
                                                    :source-map true}}
                                        {:id "channel-demo"
                                         :source-paths ["src/channel-demo" "devel/cljs"]
                                         :compiler {:output-to "target/classes/public/js/channel-demo.js"
                                                    :output-dir "target/classes/public/js/channel-demo"
                                                    :optimizations :none
                                                    :source-map true}}]}}
             :uberjar {:aot :all
                       :cljsbuild {:builds [{:id "channel-demo"
                                             :source-paths ["src/channel-demo"]
                                             :compiler {:output-to "target/classes/public/js/channel-demo.js"
                                                        :optimizations :advanced
                                                        :pretty-print false
                                                        :preamble ["react/react.min.js"]
                                                        :externs ["react/externs/react.js"]}}]}}})
