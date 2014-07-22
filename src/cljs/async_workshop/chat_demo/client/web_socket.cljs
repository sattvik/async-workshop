(ns async-workshop.chat-demo.client.web-socket
  (:require [cljs.core.async :as async :refer [put! <!]]
            [goog.events :as ev]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:import goog.net.WebSocket))

(defn ^:private make-init-state
  "Creates the initial state for the web socket widget.  In particular, this
  instantiates a WebSocket instance and creates a core.async channel for
  handling events from the WebSocket. "
  []
  (let [in (async/chan)
        socket (WebSocket.)]
    (ev/listen socket
               #js [WebSocket.EventType.CLOSED
                    WebSocket.EventType.ERROR
                    WebSocket.EventType.MESSAGE
                    WebSocket.EventType.OPENED]
               (fn [e]
                 (put! in (condp = (.-type e)
                            WebSocket.EventType.CLOSED
                              {:type :closed}
                            WebSocket.EventType.ERROR
                              {:type :error
                               :value (.-data e)}
                            WebSocket.EventType.MESSAGE
                              {:type :message
                               :value (.-message e)}
                            WebSocket.EventType.OPENED
                              {:type :opened}))))
    {:in in
     :socket socket}))

(defn ^:private event-loop
  [global-state in]
  (go-loop []
    (when-some [{:keys [type value]} (<! in)]
      (let [msg (condp = type
                  :closed "Connection to server closed"
                  :error (str "Connection error: " value)
                  :message (str "Received message: " value)
                  :opened "Connection to server established")]
        (om/transact! global-state [:chat-history] #(conj % msg))
        (recur)))))

(defn ^:private startup
  "Actually connects the web socket to the server and starts the local event
  loop."
  [global-state {:keys [in socket] :as local-state}]
  (let [doc-uri (.-location js/window)
        ws-uri (str "ws://" (.-host doc-uri) (.-pathname doc-uri) "/ws")]
    (event-loop global-state in)
    (.open socket ws-uri)))

(defn ^:private shutdown
  "Tears down the web socket connection."
  [{:keys [in socket]}]
  (.close socket)
  (async/close in))

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
      "WebSocket manager")))

