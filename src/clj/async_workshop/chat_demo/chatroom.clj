(ns async-workshop.chat-demo.chatroom
  (:require [clojure.core.async :as async :refer [<! go-loop]]
            [org.httpkit.server :as web-socket]))

(defn ^:private init-session
  [socket]
  (let [in (async/chan 1)]
    (web-socket/on-receive socket
      (fn [msg]
        (async/put! in {:type :message
                        :value msg})))
    (web-socket/on-close socket
      (fn [status]
        (async/put! in {:type :close
                        :value status})))
    (go-loop []
      (when-let [{:keys [type value]} (<! in)]
        (when (identical? :message type)
          (web-socket/send! socket value)
          (recur))))))

(defn chatroom-handler
  [req]
  (web-socket/with-channel req socket
    (init-session socket)))

