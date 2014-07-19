(ns async-workshop.server.routes
  (:require [async-workshop.server.pages :as pages]
            [async-workshop.server.pages.reference :as reference]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]))

(defroutes routes
  (GET "/" [] pages/main-page)
  (GET "/reference" [] reference/main-page)
  (GET "/reference/primitives" [] reference/primitives-page)
  (route/resources "/")
  (route/not-found pages/not-found))

(def app routes)
