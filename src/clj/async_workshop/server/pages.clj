;
; Copyright © 2014 Daniel Solano Gómez.
;
; This program is provided under the terms of the Eclipse Public License 1.0
; <http://www.eclipse.org/org/documents/epl-v10.html>.  Any use, reproduction,
; or distribution of this program constitutes recipient's acceptance of this
; license.
;

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
   :subtitle "Strange Loop 2014"})

(defpage not-found
  "templates/not-found.html"
  nav-menu
  {:title "Not found"
   :compact? true})
