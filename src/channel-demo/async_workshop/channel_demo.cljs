;
; Copyright © 2014 Daniel Solano Gómez.
;
; This program is provided under the terms of the Eclipse Public License 1.0
; <http://www.eclipse.org/org/documents/epl-v10.html>.  Any use, reproduction,
; or distribution of this program constitutes recipient's acceptance of this
; license.
;

(ns async-workshop.channel-demo
  (:require [cljs.core.async :as async]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(def demo-state (atom {:channel (async/chan)
                       :channel-id 1
                       :messages ["Created unbuffered channel #1."]}))

(defn conj-message!
  [cursor message]
  (om/transact! cursor [:messages] #(conj % message)))

(defn update-channel
  [cursor {:keys [buffer-type buffer-size]}]
  (om/transact! cursor
    (fn [{:keys [channel channel-id messages]}]
      (async/close! channel)
      (async/into [] channel)
      (let [new-id (inc channel-id)]
        {:channel (condp = buffer-type
                    :unbuffered (async/chan)
                    :fixed (async/chan buffer-size)
                    :dropping (async/chan (async/dropping-buffer buffer-size))
                    :sliding (async/chan (async/sliding-buffer buffer-size)))
         :channel-id new-id
         :messages (conj messages
                         (if (= :unbuffered buffer-type)
                           (str "Created unbuffered channel #" new-id \.)
                           (str "Created channel #" new-id " with a " (name buffer-type) " buffer of size " buffer-size \.)))}))))

(defn channel-type-widget
  [app-state owner]
  (reify
    om/IInitState
    (init-state [_]
      {:buffer-type :unbuffered
       :buffer-size 3})
    om/IRenderState
    (render-state [_ {:keys [buffer-type buffer-size] :as local-state}]
      (dom/div nil
        "Use a channel that has "
        (dom/select #js {:onChange (fn [e]
                                     (om/set-state! owner [:buffer-type] (keyword (.-value (.-target e)))))
                         :value (name buffer-type)}
          (dom/option #js {:value "unbuffered"} "no buffer (default)")
          (dom/option #js {:value "fixed"} "fixed buffer")
          (dom/option #js {:value "dropping"} "dropping buffer")
          (dom/option #js {:value "sliding"} "sliding buffer"))
        (when (not= :unbuffered buffer-type)
          (dom/span nil
            " with a size of "
            (dom/input #js {:onChange (fn [e]
                                        (om/set-state! owner [:buffer-size] (int (.-value (.-target e)))))
                            :value buffer-size
                            :min 1
                            :max 5
                            :type "number"})))
        "."
        (dom/button #js {:onClick (fn [e] (update-channel app-state local-state))}
          "Reset channel")))
    om/IDisplayName
    (display-name [_] "channel-type-widget")))

(defn messages-widget
  [cursor owner]
  (reify
    om/IRender
    (render [_]
      (js/setTimeout
        (fn []
          (when-let [ta (om/get-node owner "log")]
            (set! (.-scrollTop ta) (+ 100 (.-scrollHeight ta)))))
        0)
      (dom/textarea #js {:readOnly true
                                 :rows 10
                                 :style #js {:width "100%"
                                             :margin-top "1em"}
                                 :value (apply str (interpose \newline (:messages cursor)))
                                 :ref "log"}))
    om/IDisplayName
    (display-name [_] "messages-widget")))

(defn produce-widget
  [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:waiting? false
       :value 1})
    om/IRenderState
    (render-state [_ {:keys [id value waiting?] :as local-state}]
      (dom/button #js {:onClick (fn [_]
                                  (om/set-state! owner [:waiting?] true)
                                  (let [{:keys [channel channel-id]} @cursor]
                                    (async/put! channel
                                                {:producer-id id
                                                 :value value}
                                                (fn [v]
                                                  (om/set-state! owner [:waiting?] false)
                                                  (if-not v
                                                    (conj-message! cursor (str "Producer " id " failed to insert a value because channel #" channel-id " was closed."))
                                                    (do
                                                      (conj-message! cursor (str "Producer " id " inserted " value " into channel #" channel-id \.))
                                                      (om/update-state! owner [:value] inc)))))))
                       :disabled waiting? }
        (str "Produce " value " from " id \!)))
    om/IDisplayName
    (display-name [_]
      "produce-widget")))

(defn consume-widget
  [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:waiting? false})
    om/IRenderState
    (render-state [_ {:keys [id value waiting?] :as local-state}]
      (dom/button #js {:disabled waiting?
                       :onClick (fn [_]
                                  (om/set-state! owner [:waiting?] true)
                                  (let [{:keys [channel channel-id]} @cursor]
                                    (async/take! channel
                                                 (fn [v]
                                                   (om/set-state! owner [:waiting?] false)
                                                   (if-let [{:keys [producer-id value]} v]
                                                     (conj-message! cursor (str "Consumer " id " consumed " value " from producer " producer-id " on channel #" channel-id \.))
                                                     (conj-message! cursor (str "Consumer " id " failed consume a value because channel #" channel-id " was closed.")))))))}
        (str "Consume from " id \!)))
    om/IDisplayName
    (display-name [_]
      "produce-widget")))

(om/root channel-type-widget demo-state
  {:target (.getElementById js/document "channel-type-widget")})

(om/root messages-widget demo-state
  {:target (.getElementById js/document "messages-widget")})

(doseq [i (range 1 6)]
  (om/root produce-widget demo-state
    {:target (.getElementById js/document (str "produce-widget-" i))
     :state {:id (get [\A \B \C \D \E] (dec i))}}))

(doseq [i (range 1 6)]
  (om/root consume-widget demo-state
    {:target (.getElementById js/document (str "consume-widget-" i))
     :state {:id (get [\F \G \H \I \J] (dec i))}}))
