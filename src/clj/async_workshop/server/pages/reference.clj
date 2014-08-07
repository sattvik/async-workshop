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
            [clojure.data.json :as json]
            [clojure.repl :as repl]
            [clojure.string :as string]
            [compojure.core :refer [defroutes GET]]
            [net.cgrand.enlive-html :as el :refer [deftemplate defsnippet]])
  (:import [clojure.lang AFunction Compiler LineNumberingPushbackReader RT]
           [java.io InputStreamReader PushbackReader]))

(def clojure-only-async-vars '#{<!! >!! alts!! alt!! ioc-alts! thread-call thread})

(defn read-cljs-metas
  [file]
  (when-let [cljs-stream (.getResourceAsStream (RT/baseLoader) file)]
    (with-open [cljs-reader (LineNumberingPushbackReader. (InputStreamReader. cljs-stream))]
      (binding [*read-eval* false]
        (loop [cljs-metas []]
          (let [text (StringBuilder.)
                line-no (.getLineNumber cljs-reader)
                text-reader (proxy [PushbackReader] [cljs-reader]
                              (read []
                                (let [i (proxy-super read)]
                                  (when (not= i -1)
                                    (.append text (char i)))
                                  i))
                              (unread [i]
                                (proxy-super unread i)
                                (.deleteCharAt text (dec (.length text)))))
                form (read text-reader false ::eof)]
            (if (= form ::eof)
              cljs-metas
              (do
                (loop [i (.read cljs-reader)]
                  (cond
                    (= i -1) nil
                    (= \newline (char i)) (recur (.read cljs-reader))
                    :default (.unread cljs-reader i)))
                (if (#{'defn 'defmacro 'defprotocol} (first form))
                  (recur (conj cljs-metas
                               {:name (second form)
                                :file file
                                :column 1
                                :line line-no
                                :source (str text)}))
                  (recur cljs-metas))))))))))

(def cljs-metas (mapcat read-cljs-metas ["cljs/core/async.cljs" "cljs/core/async/macros.clj"]))
(def cljs-vars (into #{} (map #(:name %) cljs-metas)))

(defn add-id [m]
  (assoc m :id (Compiler/munge (name (:name m)))))

(defn add-var
  [{:keys [ns name] :as m}]
  (assoc m :var (ns-resolve ns name)))

(defn protocol?
  [v]
  (and (map? v)
       ((every-pred :var :on :on-interface :sigs :method-builders :method-map) v)))

(defn add-var-type
  [{:keys [var macro protocol] :as m}]
  (assoc m
         :var-type
         (cond
           macro :macro
           (protocol? @var) :protocol
           (and protocol
                (protocol? @protocol)) :protocol-method
           (isa? (class @var) AFunction) :function
           :default :unknown)))

(defn add-source-from
  [dest {:keys [ns name file line]}]
  (if-let [src (repl/source-fn (symbol (str (ns-name ns)) (str name)))]
    (assoc dest :clj-source {:file file
                             :line line
                             :source src})
    dest))

(defn add-clj-source
  [{:keys [var-type] :as m}]
  (if (= var-type :protocol-method)
    (add-source-from m (meta (:protocol m)))
    (add-source-from m m)))

(defn add-clojure-only?
  [{:keys [name var-type] :as m}]
  (assoc m :clojure-only? (and (not= :protocol-method var-type)
                               (not (cljs-vars name)))))

(defn add-cljs-source
  [{:keys [clojure-only? name var-type] :as m}]
  (if clojure-only?
    m
    (let [src-name (if (= var-type :protocol-method)
                     (:name (meta (:protocol m)))
                     name)
          src-info (some #(when (= name (:name %)) %) cljs-metas)]
      (assoc m :cljs-source src-info))))

(defn add-extra-metas
  [metas]
  (-> metas
      (add-id)
      (add-var)
      (add-var-type)
      (add-clojure-only?)
      (add-clj-source)
      (add-cljs-source)))

(def async-vars (->> 'clojure.core.async
                     ((comp vals ns-publics find-ns))
                     (map meta)
                     (map add-extra-metas)
                     (remove (fn [m] (= :protocol-method (:var-type m))))
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
  (->> (concat ["(" name]
               (if (seq arglist) [" "] [])
               (interpose " " arglist)
               [")"])
       (apply str)))

(defn render-invocations
  [name arglists]
  (json/write-str (map (partial render-invocation name) arglists)))

(defn render-sig
  [{:keys [name arglists] :as sig}]
  (map (partial render-invocation name) arglists))

(defn render-sigs
  [sigs]
  (json/write-str (mapcat render-sig sigs)))

(defn parse-doc
  [doc]
  (->> (or doc "Sorry, no documentation was found.")
       ((fn [d] (string/split d #"\n\n")))
       (map (fn [p] [:p p]))
       (el/html)))

(defn source-attribute
  [source-info]
  (when source-info
    (json/write-str (update-in source-info [:source] #(-> (string/replace % "'" "\\u0039")
                                                          (string/split-lines))))))

(defpage apidocs-page
  "templates/reference/apidocs.html"
  nav-menu
  {:title "API Documenation"
   :subtitle "core.async in depth"}
  [_]
  [:async-workshop-clojuredoc]
    (el/clone-for [{:keys [doc id var-type clojure-only? arglists] :as var-info} async-vars]
                  (el/do->
                    (el/set-attr :id id)
                    (el/set-attr :varname (:name var-info))
                    (el/set-attr :vartype (name var-type))
                    (el/set-attr :invocations (if (= :protocol var-type)
                                                (render-sigs (vals (:sigs @(:var var-info))))
                                                (render-invocations (:name var-info) arglists)))
                    (if clojure-only?
                      (el/set-attr :isclojureonly true)
                      (el/do->
                        (el/remove-attr :isclojureonly)
                        (el/set-attr :cljssource (source-attribute (:cljs-source var-info)))))
                    (el/set-attr :cljsource (source-attribute (:clj-source var-info)))
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
