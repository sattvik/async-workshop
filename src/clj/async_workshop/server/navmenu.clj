(ns async-workshop.server.navmenu
  (:require [net.cgrand.enlive-html :as enlive :refer [deftemplate defsnippet]])) 

(defsnippet item-snippet "templates/navmenu.html"
  [:paper-item]
  [{:keys [label anchor]}]
  [:paper-item] (enlive/set-attr :label label)
  [:a] (enlive/set-attr :href anchor))

(defsnippet submenu-snippet "templates/navmenu.html"
  [:core-submenu]
  [{:keys [icon label items base-uri]} {:keys [uri]}]
  [:core-submenu] (enlive/do->
                    (enlive/set-attr :label label)
                    (if icon 
                      (enlive/set-attr :icon icon)
                      (enlive/remove-attr :icon))
                    (if (= base-uri uri)
                      (enlive/set-attr :active true)
                      (enlive/remove-attr :active))
                    (enlive/content (map item-snippet items))))

(defsnippet menu-snippet "templates/navmenu.html"
  [:nav]
  [nav-menu req]
  [:core-menu] (enlive/content (map submenu-snippet nav-menu (repeat req))))
