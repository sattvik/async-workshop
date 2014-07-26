(ns async-workshop.chat-demo.client.web-socket
  (:require [goog.events :as ev]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async])
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:import goog.net.WebSocket))

(defn ^:private make-init-state
  "Creates the initial state for the web socket widget.  In particular, this
  instantiates a WebSocket instance and creates a core.async channel for
  handling events from the WebSocket. "
  []
  (let [socket (WebSocket.)
        events (async/chan)]
    (ev/listen socket
               #js [WebSocket.EventType.CLOSED
                    WebSocket.EventType.ERROR
                    WebSocket.EventType.MESSAGE
                    WebSocket.EventType.OPENED]
               (fn [e]
                 (async/put!
                   events
                   (condp = (.-type e)
                     WebSocket.EventType.CLOSED
                       {:type :socket-closed}
                     WebSocket.EventType.ERROR
                       {:type :socket-error
                        :value (.-data e)}
                     WebSocket.EventType.MESSAGE
                       {:type :rx-message
                        :value (.-message e)}
                     WebSocket.EventType.OPENED
                       {:type :socket-opened}))))
    {:socket socket
     :socket-events events}))

(defn ^:private event-loop
  [app-state socket-events socket]
  (let [tx-channel (:transmit-channel app-state)
        append-chat-history
          (fn [msg]
            (om/transact! app-state [:chat-history] #(conj % msg)))]
    (go-loop []
      (when-let [[{:keys [type value]} _] (async/alts! [socket-events tx-channel])]
        (condp = type
          :socket-opened (append-chat-history "Connection to server established.")
          :socket-closed (append-chat-history "Connection to server lost.")
          :socket-error  (append-chat-history (str "Connection error: " value))
          :rx-message    (append-chat-history value)
          :tx-message    (.send socket value)))
      (recur))))

(defn ^:private startup
  "Actually connects the web socket to the server and starts the local event
  loop."
  [global-state {:keys [socket socket-events] :as local-state}]
  (let [doc-uri (.-location js/window)
        ws-uri (str "ws://" (.-host doc-uri) (.-pathname doc-uri) "/ws")]
    (event-loop global-state socket-events socket)
    (.open socket ws-uri)))

(defn ^:private shutdown
  "Tears down the web socket connection."
  [{:keys [socket socket-events]}]
  (.close socket)
  (async/close! socket-events))

(defn ws-widget
  [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      (make-init-state))
    om/IWillMount
    (will-mount [_]
      (startup cursor (om/get-state owner)))
    om/IWillUnmount
    (will-unmount [_]
      (shutdown (om/get-state owner)))
    om/IRender
    (render [_]
      ; We must render somthing…
      (dom/span nil ""))
    om/IDisplayName
    (display-name [_]
      "ws-widget")))
