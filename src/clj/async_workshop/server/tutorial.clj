(ns async-workshop.server.tutorial
  (:require [async-workshop.server.workshop-page :refer [defpage]]
            [compojure.core :refer [defroutes GET]]))

(def tutorial-menu
  [{:label "Get started"
    :base-uri "/tutorial"
    :items [{:label "Overview"
             :anchor "overview"}
            {:label "Get the code"
             :anchor "get-the-code"}
            {:label "About the code"
             :anchor "about-the-code"}
            {:label "Develop the code"
             :anchor "develop-the-code"}]}
   {:label "A minimal client"
    :base-uri "/tutorial/a-minimal-client"
    :items [{:label "Overview"
             :anchor "overview"}
            {:label "About the app"
             :anchor "about-the-app"}
            {:label "Socket initialisation"
             :anchor "socket-init"}
            {:label "About event loops"
             :anchor "about-event-loops"}
            {:label "Write a loop"
             :anchor "write-a-loop"}
            {:label "Clean things up"
             :anchor "clean-things-up"}
            {:label "Summary"
             :anchor "summary"}]}
   {:label "A minimal server"
    :base-uri "/tutorial/a-minimal-server"
    :items [{:label "Overview"
             :anchor "overview"}
            {:label "About the server"
             :anchor "about-the-server"}
            {:label "Server WebSockets"
             :anchor "server-websockets"}
            {:label "Implement the server"
             :anchor "implement-the-server"}
            {:label "Summary"
             :anchor "summary"}]}
   {:label "Sending a message"
    :base-uri "/tutorial/sending-a-message"
    :items [{:label "Overview"
             :anchor "overview"}
            {:label "Add an input"
             :anchor "add-an-input"}
            {:label "Add a message channel"
             :anchor "add-a-message-channel"}
            {:label "Produce the message"
             :anchor "produce-the-message"}
            {:label "Consume the message"
             :anchor "consume-the-message"}
            {:label "Summary"
             :anchor "summary"}]}
   {:label "Chat together"
    :base-uri "/tutorial/chat-together"
    :items [{:label "Overview"
             :anchor "overview"}
            {:label "A publish channel"
             :anchor "a-publish-channel"}
            {:label "A subscribe channel"
             :anchor "a-subscribe-channel"}
            {:label "Add a message channel"
             :anchor "add-a-message-channel"}
            {:label "Handle message events"
             :anchor "handle-message-events"}
            {:label "Summary"
             :anchor "summary"}]}])

(defpage main-page
  "templates/tutorial/index.html"
  tutorial-menu
  {:title "The core.async tutorial"
   :subtitle "Learn by doing"})

(defpage minimal-client-page
  "templates/tutorial/a-minimal-client.html"
  tutorial-menu
  {:title "A minimal client"
   :subtitle "An introduction to core.async"})

(defpage minimal-server-page
  "templates/tutorial/a-minimal-server.html"
  tutorial-menu
  {:title "A minimal server"
   :subtitle "Event loops on the server"})

(defpage sending-a-message-page
  "templates/tutorial/sending-a-message.html"
  tutorial-menu
  {:title "Sending a message"
   :subtitle "Interact with our echo server"})

(defpage chat-together-page
  "templates/tutorial/chat-together.html"
  tutorial-menu
  {:title "Chat together"
   :subtitle "Because talking to yourself gets boring"})

(defroutes routes
  (GET "/" request main-page)
  (GET "/a-minimal-client" request minimal-client-page)
  (GET "/a-minimal-server" request minimal-server-page)
  (GET "/sending-a-message" request sending-a-message-page)
  (GET "/chat-together" request chat-together-page))
