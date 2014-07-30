(ns async-workshop.server.app
  (:require [async-workshop.chat-demo.server.routes :refer [chat-app]]
            [async-workshop.server.pages :as pages]
            [async-workshop.server.pages.reference :as reference]
            [async-workshop.server.tutorial :as tutorial]
            [compojure.core :refer [context defroutes GET routes]]
            [compojure.route :as route]))

(defroutes main-routes
  (GET "/" request pages/main-page)
  (context "/reference" request reference/routes)
  (context "/tutorial" request tutorial/routes)
  (route/resources "/")
  (route/not-found pages/not-found))

(defroutes chat-routes
  (context "/chat-demo" request chat-app))

(defn wrap-enable-chat
  [handler]
  (fn [req]
    (handler (assoc-in req [:async-workshop :enable-chat?] true))))

(defn app
  [{:keys [enable-chat?]}]
  (if enable-chat?
    (-> (routes chat-routes main-routes)
        (wrap-enable-chat))
    main-routes))
