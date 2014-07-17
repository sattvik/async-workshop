(ns async-workshop.server.routes
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]))

(defroutes routes
  (route/resources "/")
  (route/not-found "TODO: not found"))

(defn root-as-index
  [handler]
  (fn [req]
    (handler (update-in req [:uri] #(if (= "/" %) "/index.html" %)))))

(def app
  (-> routes
      root-as-index))
