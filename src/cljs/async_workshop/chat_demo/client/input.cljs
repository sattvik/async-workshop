;
; Copyright © 2014 Daniel Solano Gómez.
;
; This program is provided under the terms of the Eclipse Public License 1.0
; <http://www.eclipse.org/org/documents/epl-v10.html>.  Any use, reproduction,
; or distribution of this program constitutes recipient's acceptance of this
; license.
;

(ns async-workshop.chat-demo.client.input
  (:require [cljs.core.async :as async]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn handle-change
  [ev owner]
  (om/set-state! owner :text (.. ev -target -value)))

(defn send-message
  [app-state owner]
  (let [msg (om/get-state owner :text)]
    (.log js/console (str "Sending: " msg))
    (om/set-state! owner :text "")))

(defn chat-input-widget
  "Creates a widget that allows the user to send messages to the chat server."
  [app-state owner]
  (reify
    om/IDisplayName
    (display-name [_] "chat-input-widget")
    om/IInitState
    (init-state [_]
      {:text ""})
    om/IRenderState
    (render-state [_ state]
      (dom/div nil
        (dom/input #js {:type "text"
                        :ref "chat-input"
                        :value (:text state)
                        :onChange #(handle-change % owner)})
        (dom/button #js {:onClick #(send-message app-state owner)}
                    "Send")))))
