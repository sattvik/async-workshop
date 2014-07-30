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
  [:head :title] (enlive/content (str "Get going with core.async: " title)))

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
