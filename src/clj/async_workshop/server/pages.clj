(ns async-workshop.server.pages
  (:require [async-workshop.server.navmenu :as navmenu]
            [net.cgrand.enlive-html :as enlive :refer [deftemplate defsnippet]]))

(deftemplate workshop-page "templates/workshop-page.html"
  [request {:keys [title subtitle compact? content nav-menu] :as page-info}]
  [:async-workshop-page] (enlive/do->
                           (enlive/set-attr :collapsible (not compact?))
                           (enlive/set-attr :pageTitle title)
                           (enlive/set-attr :pageSubtitle subtitle)
                           (enlive/set-attr :enableChat (get-in request [:async-workshop :enable-chat?] false))
                           (enlive/content content nav-menu))
  [:head :title] (enlive/content (str "Get going with core.async: " title)))

(def main-nav-menu
  [{:label "Home"
    :icon "home"
    :base-uri "/"
    :items [{:label "Welcome"
             :anchor "welcome"}
            {:label "Requirements"
             :anchor "requirements"}
            {:label "Getting started"
             :anchor "gettingstarted"}]}])

(defsnippet welcome-page-content "templates/welcome-page.html"
  [:body]
  []
  [:body] enlive/unwrap)

(defn main-page
  [req]
  (workshop-page
    req
    {:title "Get going with core.asyc"
     :subtitle "Lambda Jam Chicago 2014"
     :content (welcome-page-content)
     :nav-menu (navmenu/menu-snippet main-nav-menu req)}))

(defn not-found
  [req]
  (workshop-page
    req
    {:title "Not found"
     :compact? true
     :content (enlive/html [:p "I’m sorry, but there isn’t anything here for you."])
     :nav-menu (navmenu/menu-snippet main-nav-menu req)}))
