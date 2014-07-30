(ns async-workshop.server
  (:gen-class)
  (:require [compojure.handler :refer [site]]
            [async-workshop.server.app :refer [app]]
            [org.httpkit.server :refer [run-server]]))

(def default-options
  {:port 9000})

(defn parse-args
  [args]
  (condp = (count args)
    1 {:port (Integer/valueOf (nth args 0))}
    2 {:ip   (nth args 0)
       :port (Integer/valueOf (nth args 1))}
    {}))

(defn -main [& args]
  (let [options (merge default-options (parse-args args))]
    (let [handler (site (app {}))]
      (run-server handler options))
  (println "async-workshop now listening on port" (:port options))))
