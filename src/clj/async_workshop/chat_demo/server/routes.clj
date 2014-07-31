;
; Copyright © 2014 Daniel Solano Gómez.
;
; This program is provided under the terms of the Eclipse Public License 1.0
; <http://www.eclipse.org/org/documents/epl-v10.html>.  Any use, reproduction,
; or distribution of this program constitutes recipient's acceptance of this
; license.
;

(ns async-workshop.chat-demo.server.routes
  (:require [async-workshop.chat-demo.chatroom :refer [chatroom-handler]]
            [async-workshop.chat-demo.pages :as pages]
            [async-workshop.server.pages :refer [not-found]]
            [cemerick.friend :as friend]
            [cemerick.friend.workflows :as workflows]
            [compojure.core :as compojure :refer [GET ANY defroutes]]
            [compojure.route :as route]
            [ring.util.response :refer [redirect]]))

(defroutes chatroom-routes
  (GET "/" request pages/chatroom)
  (GET "/ws" request chatroom-handler))

(defroutes chat-routes
  (GET "/" request pages/landing-page)
  (GET "/login" requiest pages/login-page)
  (compojure/context "/chatroom" request
    (friend/wrap-authorize chatroom-routes #{::chatter}))
  (friend/logout
    (ANY "/logout" request (redirect "/")))
  (route/not-found not-found))

(def chat-app
  (-> chat-routes
      (friend/authenticate {:credential-fn (fn [{:keys [username]}]
                                             {:identity username
                                              :roles [::chatter]})
                            :workflows [(workflows/interactive-form)]})))
