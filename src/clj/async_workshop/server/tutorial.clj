(ns async-workshop.server.tutorial
  (:require [async-workshop.server.navmenu :as navmenu]
            [async-workshop.server.pages :refer [workshop-page]]
            [compojure.core :refer [defroutes GET]]
            [net.cgrand.enlive-html :as el :refer [defsnippet]]))

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

(defsnippet main-page-content "templates/tutorial/index.html"
  [:template :> el/any-node]
  [])

(defn main-page
  [request]
  (workshop-page
    {:title "The core.async tutorial"
     :subtitle "Learn by doing"
     :content (main-page-content)
     :nav-menu (navmenu/menu-snippet tutorial-menu request)}))

(defsnippet minimal-client-content "templates/tutorial/a-minimal-client.html"
  [:template :> el/any-node]
  [])

(defn minimal-client-page
  [request]
  (workshop-page
    {:title "A minimal client"
     :subtitle "An introduction to core.async"
     :content (minimal-client-content)
     :nav-menu (navmenu/menu-snippet tutorial-menu request)}))

(defsnippet minimal-server-content "templates/tutorial/a-minimal-server.html"
  [:template :> el/any-node]
  [])

(defn minimal-server-page
  [request]
  (workshop-page
    {:title "A minimal server"
     :subtitle "Event loops on the server"
     :content (minimal-server-content)
     :nav-menu (navmenu/menu-snippet tutorial-menu request)}))

(defsnippet sending-a-message-content "templates/tutorial/sending-a-message.html"
  [:template :> el/any-node]
  [])

(defn sending-a-message-page
  [request]
  (workshop-page
    {:title "Sending a message"
     :subtitle "Interact with our echo server"
     :content (sending-a-message-content)
     :nav-menu (navmenu/menu-snippet tutorial-menu request)}))

(defsnippet chat-together-content "templates/tutorial/chat-together.html"
  [:template :> el/any-node]
  [])

(defn chat-together-page
  [request]
  (workshop-page
    {:title "Chat together"
     :subtitle "Because talking to yourself gets boring"
     :content (chat-together-content)
     :nav-menu (navmenu/menu-snippet tutorial-menu request)}))

(defroutes routes
  (GET "/" request main-page)
  (GET "/a-minimal-client" request minimal-client-page)
  (GET "/a-minimal-server" request minimal-server-page)
  (GET "/sending-a-message" request sending-a-message-page)
  (GET "/chat-together" request chat-together-page))
