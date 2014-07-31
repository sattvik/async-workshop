;
; Copyright © 2014 Daniel Solano Gómez.
;
; This program is provided under the terms of the Eclipse Public License 1.0
; <http://www.eclipse.org/org/documents/epl-v10.html>.  Any use, reproduction,
; or distribution of this program constitutes recipient's acceptance of this
; license.
;

(ns async-workshop.chat-demo.pages
  (:require [async-workshop.server.workshop-page :refer [defpage]]
            [cemerick.friend :as friend]
            [net.cgrand.enlive-html :as el]))

(defpage landing-page
  "templates/chat/landing-page.html"
  []
  {:title "Chat demo"
   :subtitle "Chat with core.async"})

(defpage login-page
  "templates/chat/login-page.html"
  []
  {:title "Chat demo"
   :subtitle "Login"})

(defpage chatroom
  "templates/chat/chatroom.html"
  []
  {:title "Chat room"
   :compact? true}
  [req]
  [:h2] (el/content "Welcome to the chat, "
                    (get-in req [:session
                                 ::friend/identity
                                 :current])))
