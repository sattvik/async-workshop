(ns async-workshop.server.routes
  (:require [async-workshop.server.pages :as pages]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]))

(defroutes routes
  (route/resources "/")
  (route/not-found (pages/workshop-page)))

(defn root-as-index
  [handler]
  (fn [req]
    (handler (update-in req [:uri] #(if (= "/" %) "/index.html" %)))))

(def app
  (-> routes
      root-as-index))
