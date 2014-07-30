(ns async-workshop.server.pages.reference
  (:require [async-workshop.server.navmenu :as navmenu]
            [async-workshop.server.pages :refer [workshop-page]]
            [clojure.core.async :as async]
            [clojure.string :as string]
            [net.cgrand.enlive-html :as el :refer [deftemplate defsnippet]]))

(def clojure-only-async-vars '#{<!! >!! alts!! alt!! ioc-alts! thread-call thread})

(def async-vars (->> 'clojure.core.async
                     ((comp vals ns-publics find-ns))
                     (map meta)
                     (map (fn [m] (assoc m :id (clojure.lang.Compiler/munge (name (:name m))))))
                     (filter :arglists)
                     (sort-by (comp name :name))))

(def nav-menu
  [{:label "API Docs"
    :icon "settings"
    :base-uri "/reference/apidocs"
    :items (mapv (fn [meta]
                   {:label (name (:name meta))
                    :anchor (:id meta)})
                 async-vars)}
   #_{:label "Patterns"
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
    req
    {:title "Reference"
     :subtitle "Don't panic."
     :current-section "reference"
     :content (main-page-content)
     :nav-menu (navmenu/menu-snippet nav-menu req)}))

(defn render-invocation
  [name arglist]
  (->> (concat ["(" name " "]
               (interpose " " arglist)
               [")"])
       (apply str)))

(defn render-invocations
  [name arglists]
  (->> (map (partial render-invocation name) arglists)
       (interpose \newline)
       (apply str)))

(defn parse-doc
  [doc]
  (->> (or doc "Sorry, no documentation was found.")
       ((fn [d] (string/split d #"\n\n")))
       (map (fn [p] [:p p]))
       (el/html)))

(defsnippet apidocs-page-content "templates/reference/apidocs.html"
  [:body]
  []
  [:body] el/unwrap
  [:async-workshop-clojuredoc] (el/clone-for [{:keys [doc id name macro arglists]} async-vars]
                                 (el/do->
                                   (el/set-attr :id id)
                                   (el/set-attr :varname name)
                                   (el/set-attr :ismacro macro)
                                   (el/set-attr :arglists (render-invocations name arglists))
                                   (el/set-attr :clojureonly (clojure-only-async-vars name))
                                   (el/content (parse-doc doc)))))

(defn apidocs-page
  [req]
  (workshop-page
    req
    {:title "API Documenation"
     :subtitle "core.async in depth"
     :current-section "reference"
     :content (apidocs-page-content)
     :nav-menu (navmenu/menu-snippet nav-menu req)}))
