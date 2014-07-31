;
; Copyright © 2014 Daniel Solano Gómez.
;
; This program is provided under the terms of the Eclipse Public License 1.0
; <http://www.eclipse.org/org/documents/epl-v10.html>.  Any use, reproduction,
; or distribution of this program constitutes recipient's acceptance of this
; license.
;

(ns async-workshop.chat-demo.chatroom
  (:require [cemerick.friend :as friend]
            [org.httpkit.server :as web-socket]))

(defn chatroom-handler
  [req]
  (let [user (get-in req [:session ::friend/identity :current])]
    (web-socket/with-channel req socket
      (println user "logged in"))))
