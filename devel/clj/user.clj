(ns user
  (:require [async-workshop.server.routes :refer [app]]
            [cemerick.austin :as austin]
            [cemerick.austin.repls :refer [browser-connected-repl-js browser-repl-env]]
            [clojure.java.io :as jio]
            [compojure.handler :refer [site]]
            [net.cgrand.enlive-html :as enlive]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.reload :as reload]
            [ring.middleware.file :refer [file-request wrap-file]]))

(def options
  {:port 9000})

(defonce server (atom nil))
(defonce repl-env (atom nil))

(defn is-html-response?
  [response]
  (= "text/html" (get-in response [:headers "Content-Type"])))

(defn strip-content-length
  [response]
  (update-in response [:headers]
             (fn [headers]
               (dissoc headers "Content-Length"))))

(defn add-austin-script-tag
  [response]
  (update-in response [:body]
             (fn [body]
               ((enlive/template (enlive/html-resource body)
                                 []
                                 [:body] (enlive/append
                                           (enlive/html [:script {:src "browser_repl.js"}]
                                                        [:script (browser-connected-repl-js)])))))))

(defn wrap-austin
  [handler]
  (fn [request]
    (let [response (handler request)]
      (if (is-html-response? response)
        (-> response
            (strip-content-length)
            (add-austin-script-tag))
        response))))

(defn wrap-serve-dir
  [handler dir]
  (if (.isDirectory (jio/file dir))
    (-> handler
        (wrap-file dir))
    handler))

(defn wrap-serve-bower-components-dir
  [handler]
  (let [components-dir "../bower_components"]
    (if (.isDirectory (jio/file components-dir))
      (fn [{:keys [uri] :as req}]
        (if-let [[_ component-path] (re-matches #"^/components(/.*)" uri)]
          (or (file-request (assoc req :uri component-path)
                            components-dir)
              (handler req))
          (handler req)))
      handler)))

(defn wrap-generator-dirs
  [handler]
  (-> handler
      (wrap-serve-dir "../src")
      (wrap-serve-dir "../build")
      (wrap-serve-bower-components-dir)))

(defn start-server
  [server]
  (if-not server
    (run-server (-> (site #'app)
                    (wrap-generator-dirs)
                    (wrap-austin)
                    (reload/wrap-reload {:dirs ["src/clj" "devel/clj"]}))
                options)
    server))

(defn stop-server
  [server]
  (when server
    (server)))

(defn init-repl-env
  [repl-env]
  (if-not repl-env
    (reset! browser-repl-env (austin/repl-env))
    repl-env))

(defn start []
  (swap! repl-env init-repl-env)
  (swap! server start-server))

(defn stop []
  (swap! server stop-server))
