;
; Copyright © 2014 Daniel Solano Gómez.
;
; This program is provided under the terms of the Eclipse Public License 1.0
; <http://www.eclipse.org/org/documents/epl-v10.html>.  Any use, reproduction,
; or distribution of this program constitutes recipient's acceptance of this
; license.
;

(ns async-workshop.server.pages.reference
  (:require [async-workshop.server.navmenu :as navmenu]
            [async-workshop.server.workshop-page :refer [defpage]]
            [clojure.core.async :as async]
            [clojure.string :as string]
            [compojure.core :refer [defroutes GET]]
            [net.cgrand.enlive-html :as el :refer [deftemplate defsnippet]]))

(def clojure-only-async-vars '#{<!! >!! alts!! alt!! ioc-alts! thread-call thread})

(def async-vars (->> 'clojure.core.async
                     ((comp vals ns-publics find-ns))
                     (map meta)
                     (map (fn [m] (assoc m :id (clojure.lang.Compiler/munge (name (:name m))))))
                     (filter :arglists)
                     (sort-by (comp name :name))))

(def nav-menu
  [{:label "Primitives"
    :icon "extension"
    :base-uri "/reference/primitives"
    :items [{:label "About core.async"
             :anchor "about"}
            {:label "Channels"
             :anchor "channels"}
            {:label "Buffered channels"
             :anchor "buffered-channels"}
            {:label "Basic operations"
             :anchor "basic-channel-ops"}
            {:label "The go macro"
             :anchor "go-macro"}]}
   #_{:label "Patterns"
    :icon "view-quilt"
    :base-uri "/reference/patterns"
    :items []}
   {:label "API"
    :icon "settings"
    :base-uri "/reference/apidocs"
    :items (mapv (fn [meta]
                   {:label (name (:name meta))
                    :anchor (:id meta)})
                 async-vars)}
   #_{:label "FAQ"
    :icon "help"
    :base-uri "/reference/faq"
    :items []}
   #_{:label "External resources"
    :icon "language"
    :items []}])

(defpage main-page
  "templates/reference/index.html"
  nav-menu
  {:title "Reference"
   :subtitle "Don't panic."})

(defn render-invocation
  [name arglist]
  (->> (concat ["\"(" name]
               (if (seq arglist) [" "] [])
               (interpose " " arglist)
               [")\""])
       (apply str)))

(defn render-invocations
  [name arglists]
  (str \[
       (->> (map (partial render-invocation name) arglists)
            (interpose ", ")
            (apply str))
       \]))

(defn parse-doc
  [doc]
  (->> (or doc "Sorry, no documentation was found.")
       ((fn [d] (string/split d #"\n\n")))
       (map (fn [p] [:p p]))
       (el/html)))

(defpage apidocs-page
  "templates/reference/apidocs.html"
  nav-menu
  {:title "API Documenation"
   :subtitle "core.async in depth"}
  [_]
  [:async-workshop-clojuredoc]
    (el/clone-for [{:keys [doc id name macro arglists]} async-vars]
                  (el/do->
                    (el/set-attr :id id)
                    (el/set-attr :varname name)
                    (el/set-attr :ismacro macro)
                    (el/set-attr :arglists (render-invocations name arglists))
                    (el/set-attr :clojureonly (clojure-only-async-vars name))
                    (el/content (parse-doc doc)))))

(defpage primitives-page
  "templates/reference/primitives.html"
  nav-menu
  {:title "core.async primitives"
   :subtitle "Understanding building blocks"})

(defroutes routes
  (GET "/" request main-page)
  (GET "/apidocs" request apidocs-page)
  (GET "/primitives" request primitives-page))
