;
; Copyright © 2014 Daniel Solano Gómez.
;
; This program is provided under the terms of the Eclipse Public License 1.0
; <http://www.eclipse.org/org/documents/epl-v10.html>.  Any use, reproduction,
; or distribution of this program constitutes recipient's acceptance of this
; license.
;

(ns async-workshop.server.navmenu
  (:require [net.cgrand.enlive-html :as enlive :refer [deftemplate defsnippet]])) 

(defsnippet item-snippet "templates/navmenu.html"
  [:paper-item]
  [{:keys [label anchor]} base-uri]
  [:paper-item] (enlive/set-attr :label label)
  [:a] (enlive/set-attr :href (str base-uri "#" anchor)))

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
                    (enlive/content (map item-snippet items (repeat base-uri)))))

(defsnippet menu-snippet "templates/navmenu.html"
  [:nav]
  [nav-menu req]
  [:core-menu] (enlive/content (map submenu-snippet nav-menu (repeat req))))
