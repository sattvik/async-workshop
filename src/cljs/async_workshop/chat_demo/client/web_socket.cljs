;
; Copyright © 2014 Daniel Solano Gómez.
;
; This program is provided under the terms of the Eclipse Public License 1.0
; <http://www.eclipse.org/org/documents/epl-v10.html>.  Any use, reproduction,
; or distribution of this program constitutes recipient's acceptance of this
; license.
;

(ns async-workshop.chat-demo.client.web-socket
  (:require [goog.events :as ev]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
  (:import goog.net.WebSocket))

(defn ^:private make-init-state
  "Creates the initial state for the web socket widget.  In particular, this
  instantiates a WebSocket instance and creates a core.async channel for
  handling events from the WebSocket. "
  []
  (let [socket (WebSocket.)]
    (ev/listen socket
               #js [WebSocket.EventType.CLOSED
                    WebSocket.EventType.ERROR
                    WebSocket.EventType.MESSAGE
                    WebSocket.EventType.OPENED]
               (fn [e]
                 (.log js/console (.-type e))))
    {:socket socket}))

(defn ^:private startup
  "Actually connects the web socket to the server and starts the local event
  loop."
  [global-state {:keys [socket] :as local-state}]
  (let [doc-uri (.-location js/window)
        ws-uri (str "ws://" (.-host doc-uri) (.-pathname doc-uri) "/ws")]
    (.open socket ws-uri)))

(defn ^:private shutdown
  "Tears down the web socket connection."
  [{:keys [socket]}]
  (.close socket))

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
