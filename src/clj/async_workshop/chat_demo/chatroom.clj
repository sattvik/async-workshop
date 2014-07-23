(ns async-workshop.chat-demo.chatroom
  (:require [cemerick.friend :as friend]
            [org.httpkit.server :as web-socket]))

(defn chatroom-handler
  [req]
  (let [user (get-in req [:session ::friend/identity :current])]
    (web-socket/with-channel req socket
      (println user "logged in"))))
