;
; Copyright © 2014 Daniel Solano Gómez.
;
; This program is provided under the terms of the Eclipse Public License 1.0
; <http://www.eclipse.org/org/documents/epl-v10.html>.  Any use, reproduction,
; or distribution of this program constitutes recipient's acceptance of this
; license.
;

(ns async-workshop.channel-demo
  (:require [cljs.core.async :as async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn $
  [element id]
  (aget (aget element "$") (clj->js id)))

(defn buffer-type-updated
  [{:keys [new-value old-value element] :as event}]
  (if (some #(= "unbuffered" %) [old-value new-value])
    (let [buffer-size ($ element :buffer-size)
          animation ($ element :fadein)
          [dir opacity] (if (= new-value "unbuffered")
                          ["reverse" 0]
                          ["normal" 1])]
      (set! (.-direction animation) dir)
      (set! (.-target animation) buffer-size)
      (.play animation)
      (set! (.-opacity (.-style buffer-size)) opacity))))

(defn event-loop
  [element]
  (let [events (aget element "events")
        messages (aget element "messages")
        messages-element ($ element :messages)
        post-message #(.push messages %)]
    (go-loop []
      (when-let [{:keys [type] :as event} (<! events)]
        (condp = type
          :buffer-type-changed
            (buffer-type-updated event)
          :produce-completed
            (let [{:keys [model value producer-id channel-id]} event]
              (aset model "waiting" false)
              (aset model "next" (inc value))
              (post-message (str "Producer " producer-id " put " value " into channel #" channel-id \.)))
          :produce-failed
            (let [{:keys [model value producer-id channel-id]} event]
              (aset model "waiting" false)
              (post-message (str "Producer " producer-id " failed to put into channel #" channel-id " because it was closed.")))
          :consume-completed
            (let [{:keys [model value producer-id consumer-id channel-id]} event]
              (aset model "waiting" false)
              (post-message (str "Consumer " consumer-id " took value " value " from producer " producer-id " on channel #" channel-id \.)))
          :consume-failed
            (let [{:keys [model value consumer-id channel-id]} event]
              (aset model "waiting" false)
              (post-message (str "Consumer " consumer-id " failed to take from channel #" channel-id " because it was closed.")))
          :close-channel
            (let [{:keys [channel channel-id]} event]
              (post-message (str "Draining and closing channel #" channel-id \.))
              (async/close! channel)
              (let [drain (async/map< #(assoc % :type :channel-drained :channel-id channel-id)
                                      channel)]
                (async/pipe drain events false)))
          :channel-drained
            (let [{:keys [producer-id channel-id value]} event]
              (post-message (str "Value " value " from producer " producer-id " drained from channel #" channel-id \.)))
          :create-channel
            (let [{:keys [buffer-size buffer-type channel-id demo-channel]} event]
              (aset demo-channel "channel-id" channel-id)
              (aset demo-channel "channel"
                    (condp = buffer-type
                      :unbuffered (async/chan)
                      :fixed (async/chan buffer-size)
                      :dropping (async/chan (async/dropping-buffer buffer-size))
                      :sliding (async/chan (async/sliding-buffer buffer-size))))
              (post-message
                (if (= :unbuffered buffer-type)
                  (str "Created unbuffered channel #" channel-id \.)
                  (str "Created channel #" channel-id " with " (name buffer-type) " buffer of size " buffer-size \.)))))
        (recur)))))

(defn on-create
  [element]
  (doto element
    (aset "events" (async/chan))
    (aset "demo-channel" #js {:channel (async/chan)
                              :channel-id 1})
    (aset "producers" (apply array
                             (for [i (range 1 6)]
                               #js {:id (char (+ i 64))
                                    :next (* i 100)
                                    :waiting false})))
    (aset "consumers" (apply array
                             (for [i (range 1 6)]
                               #js {:id (char (+ 64 i))
                                    :waiting false})))
    (aset "messages" #js ["Created unbuffered channel #1."])))

(defn signal-buffer-type-changed
  [element old-val new-val]
  (let [events (aget element "events")]
    (async/put! events
                {:type :buffer-type-changed
                 :old-value old-val
                 :new-value new-val
                 :element element})))

(defn consume
  [polymer-element event sender]
  (let [[channel channel-id] (map #(aget (aget polymer-element "demo-channel") %)
                                  (array "channel" "channel-id"))
        event-channel (aget polymer-element "events")
        model (-> event
                  (aget "target")
                  (aget "templateInstance")
                  (aget "model")
                  (aget "c"))]
    (aset model "waiting" true)
    (async/take! channel
                 (fn [v]
                   (let [base {:channel-id channel-id
                               :consumer-id (aget model "id")
                               :model model}]
                     (async/put! event-channel
                                 (if-let [{:keys [producer-id value]} v]
                                   (assoc base
                                          :type :consume-completed
                                          :producer-id producer-id
                                          :value value)
                                   (assoc base :type :consume-failed))))))))

(defn produce
  [polymer-element event sender]
  (let [[channel channel-id] (map #(aget (aget polymer-element "demo-channel") %)
                                  (array "channel" "channel-id"))
        event-channel (aget polymer-element "events")
        model (-> event
                  (aget "target")
                  (aget "templateInstance")
                  (aget "model")
                  (aget "p"))
        value (int (aget model "next"))]
    (aset model "waiting" true)
    (async/put! channel
                {:producer-id (aget model "id")
                 :value value}
                (fn [v]
                  (async/put! event-channel
                              {:type (if v :produce-completed :produce-failed)
                               :channel-id channel-id
                               :producer-id (aget model "id")
                               :value value
                               :model model})))))

(defn close-channel
  [polymer-element]
  (let [demo-channel (aget polymer-element "demo-channel")
        events (aget polymer-element "events")]
    (async/put! events
                {:type :close-channel
                 :channel (aget demo-channel "channel")
                 :channel-id (aget demo-channel "channel-id")})))

(defn reset-channel
  [polymer-element]
  (close-channel polymer-element)
  (let [demo-channel (aget polymer-element "demo-channel")
        events (aget polymer-element "events")]
    (async/put! events
                {:type :create-channel
                 :buffer-size (int (aget polymer-element "bufferSize"))
                 :buffer-type (keyword (aget polymer-element "bufferType"))
                 :channel-id (inc (int (aget demo-channel "channel-id")))
                 :demo-channel demo-channel})))

(defn scroll-message-window
  [polymer-element]
  (let [messages-list ($ polymer-element :messages)]
    (.setTimeout js/window
                 #(aset messages-list
                        "scrollTop"
                        (aget messages-list "scrollHeight"))
                 0)))

(js/Polymer
  "async-workshop-channel-demo"
  #js {:publish #js {:bufferType "unbuffered"
                     :bufferSize 3
                     :invalidBufferSize false
                     :producers nil
                     :consumers nil
                     :messages nil}
       :bufferTypeChanged #(signal-buffer-type-changed (js* "this") %1 %2)
       :messagesChanged #(scroll-message-window (js* "this"))
       :events nil
       :demo-channel nil
       :created #(on-create (js* "this"))
       :attached #(event-loop (js* "this"))
       :detached #(let [this (js* "this")] (async/close! (aget this "events")))
       :bufferSizeValidated #(let [this (js* "this")]
                               (aset this "invalidBufferSize" (aget ($ this :buffer-size) "invalid")))
       :consume #(consume (js* "this") %1 %3)
       :produce #(produce (js* "this") %1 %3)
       :closeChannel #(close-channel (js* "this"))
       :resetChannel #(reset-channel (js* "this"))})
