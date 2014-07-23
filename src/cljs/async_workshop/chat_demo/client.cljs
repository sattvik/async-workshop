(ns async-workshop.chat-demo.client
  (:require [async-workshop.chat-demo.client.web-socket :refer [ws-widget]]
            [clojure.browser.repl]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(def app-state (atom {:chat-history []}))

(defn chat-history-widget
  [app-state owner]
  (reify
    om/IRender
    (render [_]
      (apply dom/ul nil
             (map #(dom/li nil %)
                  (:chat-history app-state))))
    om/IDisplayName
    (display-name [_] "Chat history renderer")))

(om/root chat-history-widget app-state
  {:target (.getElementById js/document "chatHistoryWidget")})

(om/root ws-widget app-state
  {:target (.getElementById js/document "socketWidget")})
