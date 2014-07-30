(ns async-workshop.chat-demo.pages
  (:require [async-workshop.server.pages :refer [workshop-page]]
            [cemerick.friend :as friend]
            [net.cgrand.enlive-html :as el :refer [defsnippet]]))

(defsnippet landing-page-content "templates/chat/landing-page.html"
  [:template :> el/any-node]
  [_])

(defn landing-page
  [req]
  (workshop-page
    req
    {:title "Chat demo"
     :subtitle "Chat with core.async"
     :content (landing-page-content req)}))

(defsnippet login-page-content "templates/chat/login-page.html"
  [:template :> el/any-node]
  [_])

(defn login-page
  [req]
  (workshop-page
    req
    {:title "Chat demo"
     :subtitle "Login"
     :content (login-page-content req)}))

(defsnippet chatroom-content "templates/chat/chatroom.html"
  [:template :> el/any-node]
  [{:keys [chatname]}]
  [:h2] (el/content "Welcome to the chat, " chatname))

(defn chatroom
  [req]
  (workshop-page
    req
    {:tatle "Chat room"
     :compact? true
     :content (chatroom-content
                (assoc req :chatname (get-in req [:session
                                                  ::friend/identity
                                                  :current])))}))
