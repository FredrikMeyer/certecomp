(ns certecomp.adapter
  (:require
   [reitit.coercion.spec]
   [taoensso.timbre :as t :refer [info]]
   [ring.adapter.jetty :as jetty]))

(defn app-server [handler {:keys [port]}]
  (info (str "Inits handler on port " port))
  (jetty/run-jetty handler
                   {:port port :join? false}))

(defn stop-server [server]
  (.stop server))
