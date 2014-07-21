(ns user
  (:require [async-workshop.server.routes :refer [app]]
            [cemerick.austin :as austin]
            [cemerick.austin.repls :refer [browser-connected-repl-js browser-repl-env]]
            [clojure.java.io :as jio]
            [compojure.handler :refer [site]]
            [net.cgrand.enlive-html :as enlive]
            [net.cgrand.reload :as enlive-reload]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.reload :as reload]
            [ring.middleware.file :refer [file-request wrap-file]]))

(def options
  {:port 9000})

(defonce server (atom nil))
(defonce repl-env (atom nil))

(defn is-html-response?
  [response]
  (when-let [content-type (get-in response [:headers "Content-Type"])]
    (re-matches #".*text/html.*" content-type)))

(defn is-component?
  [request]
  (re-matches #"^/components/.*" (:uri request)))

(defn strip-content-length
  [response]
  (update-in response [:headers]
             (fn [headers]
               (dissoc headers "Content-Length"))))

(defn add-austin-script-tag
  [{:keys [body] :as response}]
  (assoc response :body
         (enlive/emit*
           (enlive/at (enlive/html-snippet (apply str body))
                      [:body] (enlive/append
                                (enlive/html [:script (browser-connected-repl-js)]))))))

(defn wrap-austin
  [handler]
  (fn [request]
    (let [response (handler request)]
      (if (and (not (is-component? request))
               (is-html-response? response))
        (-> response
            (strip-content-length)
            (add-austin-script-tag))
        response))))

(defn start-server
  [server]
  (if-not server
    (run-server (-> (site #'app)
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
  (dorun (map (comp enlive-reload/auto-reload find-ns)
              '[async-workshop.server.pages
                async-workshop.server.pages.reference
                async-workshop.chat-demo.pages]))
  (swap! repl-env init-repl-env)
  (swap! server start-server))

(defn stop []
  (swap! server stop-server))
