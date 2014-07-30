(ns async-workshop.server.pages
  (:require [async-workshop.server.workshop-page :refer [defpage]]))

(def ^:private nav-menu
  [{:label "Home"
    :icon "home"
    :base-uri "/"
    :items [{:label "Welcome"
             :anchor "welcome"}
            {:label "Requirements"
             :anchor "requirements"}
            {:label "Getting started"
             :anchor "gettingstarted"}]}])

(defpage main-page
  "templates/index.html"
  nav-menu
  {:title "Get going with core.asyc"
   :subtitle "Lambda Jam Chicago 2014"})

(defpage not-found
  "templates/not-found.html"
  nav-menu
  {:title "Not found"
   :compact? true})
