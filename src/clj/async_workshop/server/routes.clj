(ns async-workshop.server.routes
  (:require [async-workshop.server.pages :as pages]
            [async-workshop.server.pages.reference :as reference]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]))

(defroutes routes
  (GET "/" request (pages/main-page request))
  (GET "/reference" request (reference/main-page request))
  (GET "/reference/apidocs" request (reference/apidocs-page request))
  (route/resources "/")
  (route/not-found pages/not-found))

(def app routes)
