;
; Copyright © 2014 Daniel Solano Gómez.
;
; This program is provided under the terms of the Eclipse Public License 1.0
; <http://www.eclipse.org/org/documents/epl-v10.html>.  Any use, reproduction,
; or distribution of this program constitutes recipient's acceptance of this
; license.
;

(ns async-workshop.chat-demo.chatroom
  (:require [clojure.core.async :as async :refer [alts! >! go]]
            [cemerick.friend :as friend]
            [org.httpkit.server :as web-socket]))

(def publish-channel (async/chan 128))
(def subscribe-channel (async/mult publish-channel))
(def chatters (atom {}))

(defn client-loop
  [socket-events socket username]
  (let [chat-events (async/chan (async/dropping-buffer 32))
        uuid (str (java.util.UUID/randomUUID))]
    (->> @chatters
         (map second)
         (interpose ", ")
         (apply str "The following people are here: ")
         (web-socket/send! socket))
    (swap! chatters assoc uuid username)
    (async/tap subscribe-channel chat-events)
    (go
      (>! publish-channel {:type :chat-message
                           :value (str username " has joined the chat")})
      (loop []
        (let [[{:keys [type value] :as event} _] (alts! [socket-events
                                                         chat-events])]
          (cond
            (identical? type :socket-received)
              (do
                (>! publish-channel {:type :chat-message
                                     :value (str username ": " value)})
                (recur))
            (identical? type :chat-message)
              (do
                (web-socket/send! socket value)
                (recur))
            :default
              (do
                (>! publish-channel {:type :chat-message
                                     :value (str username " has left the chat")})
                (swap! chatters dissoc uuid)
                (async/untap subscribe-channel chat-events)
                (async/close! chat-events)
                (async/close! socket-events)
                (web-socket/close socket))))))))

(defn ^:private init-session
  [socket username]
  (let [in (async/chan 1)]
    (web-socket/on-receive socket
      (fn [msg]
        (async/put! in {:type :socket-received
                        :value msg})))
    (web-socket/on-close socket
      (fn [status]
        (async/put! in {:type :socket-closed
                        :value status})))
    (client-loop in socket username)))

(defn chatroom-handler
  [req]
  (web-socket/with-channel req socket
    (init-session socket (get-in req [:session
                                      ::friend/identity
                                      :current]))))
