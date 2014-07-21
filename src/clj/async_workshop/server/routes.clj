(ns async-workshop.server.routes
  (:require [async-workshop.chat-demo.server.routes :refer [chat-app]]
            [async-workshop.server.pages :as pages]
            [async-workshop.server.pages.reference :as reference]
            [compojure.core :refer [context defroutes GET]]
            [compojure.route :as route]))

(defroutes routes
  (GET "/" request (pages/main-page request))
  (context "/chat-demo" request chat-app)
  (GET "/reference" request (reference/main-page request))
  (GET "/reference/apidocs" request (reference/apidocs-page request))
  (route/resources "/")
  (route/not-found pages/not-found))

(def app routes)
