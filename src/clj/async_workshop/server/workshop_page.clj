;
; Copyright © 2014 Daniel Solano Gómez.
;
; This program is provided under the terms of the Eclipse Public License 1.0
; <http://www.eclipse.org/org/documents/epl-v10.html>.  Any use, reproduction,
; or distribution of this program constitutes recipient's acceptance of this
; license.
;

(ns async-workshop.server.workshop-page
  (:require [async-workshop.server.navmenu :as navmenu]
            [net.cgrand.enlive-html :as enlive]))

(enlive/deftemplate workshop-page "templates/workshop-page.html"
  [request {:keys [title subtitle compact? content nav-menu] :as page-info}]
  [:async-workshop-page] (enlive/do->
                           (enlive/set-attr :collapsible (not compact?))
                           (enlive/set-attr :pageTitle title)
                           (enlive/set-attr :pageSubtitle subtitle)
                           (enlive/set-attr :enableChat (get-in request [:async-workshop :enable-chat?] false))
                           (enlive/content (content request) (nav-menu request)))
  [:head :title] (enlive/content (str "Get going with core.async: " title))
  [:div#preload-mid :span] (enlive/content title))

(defmacro defpage
  ([name source menu options content-args & content-transforms]
   (let [content-name (symbol (str name "-content"))]
     `(do
        (enlive/defsnippet ~content-name ~source
          [:template :> enlive/any-node]
          ~content-args
          ~@content-transforms)
        (defn ~name [request#]
          (workshop-page
            request#
            (assoc ~options
                   :content ~content-name
                   :nav-menu (partial navmenu/menu-snippet ~menu)))))))
  ([name source menu options]
   `(defpage ~name ~source ~menu ~options [~'_])))
