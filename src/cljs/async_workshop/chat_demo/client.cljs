(ns async-workshop.chat-demo.client
  (:require [clojure.browser.repl]
            [cljs.core.async :as async :refer [put! <!]]
            [goog.events :as ev]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:import goog.net.WebSocket))

(enable-console-print!)

(def app-state (atom {:web-socket {:log []}}))

(defn web-socket-monitor [data owner]
  (reify
    om/IInitState
    (init-state [_]
      (println "Initialising state…")
      (let [log-channel (async/chan)
            web-socket (WebSocket.)]
        (ev/listen web-socket
                   #js [WebSocket.EventType.CLOSED
                        WebSocket.EventType.ERROR
                        WebSocket.EventType.MESSAGE
                        WebSocket.EventType.OPENED]
                   (fn [e]
                     (.log js/console e)
                     (put! log-channel e)))
        {:log-channel log-channel
         :web-socket web-socket}))
    om/IWillMount
    (will-mount [_]
      (println "Mounting…")
      (let [uri window.location
            ws-uri (str "wss://" (.-host uri) "/" (.-pathname uri) "/ws")
            ws (om/get-state owner :web-socket)
            lc (om/get-state owner :log-channel)]
        (go (loop []
              (let [ev  (<! lc)
                    msg (condp = (.-type ev)
                          WebSocket.EventType.CLOSED "closed"
                          WebSocket.EventType.ERROR "error"
                          WebSocket.EventType.MESSAGE "message"
                          WebSocket.EventType.OPENED "opened")]
                    (om/transact! data [:web-socket :log] #(conj % msg))
                    (recur))))
        (.open ws ws-uri)))
    om/IWillUnmount
    (will-unmount [_]
      (println "Unounting…")
      (let [ws (om/get-state owner :web-socket)
            lc (om/get-state owner :log-channel)]
        (.close ws)
        (async/close lc)))
    om/IRender
    (render  [_]
      (println "Rendering…")
      (apply dom/ul nil
             (map #(dom/li nil %) (get-in data [:web-socket :log]))))))

(om/root web-socket-monitor app-state
  {:target (.getElementById js/document "chatWidget")})
