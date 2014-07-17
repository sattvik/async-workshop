(ns async-workshop.server
  (:gen-class)
  (:require [compojure.handler :refer [site]]
            [async-workshop.server.routes :refer [app]]
            [org.httpkit.server :refer [run-server]]))

(defn -main [& args]
  (let [handler (site #'app)]
    (run-server handler {:port 9000}))
  (println "async-workshop now listening on port 9000"))
