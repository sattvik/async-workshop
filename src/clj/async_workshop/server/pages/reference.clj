(ns async-workshop.server.pages.reference
  (:require [async-workshop.server.navmenu :as navmenu]
            [async-workshop.server.pages :refer [workshop-page]]
            [net.cgrand.enlive-html :as el :refer [deftemplate defsnippet]]
            [clojure.core.async :as async])) 

(def nav-menu
  [{:label "Primitives"
    :icon "settings"
    :base-uri "/reference/primitives" 
    :items []}
   
   {:label "Patterns"
    :icon "view-quilt"
    :base-uri "/reference/patterns"
    :items []}]) 

(defsnippet main-page-content "templates/reference/index.html"
  [:body]
  []
  [:body] el/unwrap)

(defn main-page
  [req]
  (workshop-page
    {:title "Reference"
     :subtitle "Don't panic."
     :current-section "reference"
     :content (main-page-content)
     :nav-menu (navmenu/menu-snippet nav-menu req)}))

(defn primitives-page
  [req]
  (workshop-page
    {:title "Primitives"
     :subtitle "The core.async API"
     :current-section "reference"
     :content (el/html [:pre (pr-str (map meta (vals (ns-publics (find-ns 'clojure.core.async)))))])
     :nav-menu (navmenu/menu-snippet nav-menu req)}))
