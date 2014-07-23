(ns async-workshop.chat-demo.chatroom
  (:require [cemerick.friend :as friend]
            [clojure.core.async :as async]
            [org.httpkit.server :as web-socket]))

(defn client-loop
  [socket-events socket username]
  (async/go
    (web-socket/send! socket (str "Hello, " username \!))
    (loop []
      (let [{:keys [type value] :as event} (async/<! socket-events)]
        (cond
          (identical? type :socket-received)
            (do
              (web-socket/send! socket value)
              (recur))
          :default
            (do
              (async/close! socket-events)
              (web-socket/close socket)))))))

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
  (let [user (get-in req [:session ::friend/identity :current])]
    (web-socket/with-channel req socket
      (init-session socket user))))
