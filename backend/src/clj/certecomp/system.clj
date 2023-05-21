(ns certecomp.system
  (:require [integrant.core :as ig]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [certecomp.adapter :as adapter]
            [certecomp.api :as api]))

(def config {:app/config {}
             :app/handler {:db (ig/ref :app/db)}
             :app/adapter {:config (ig/ref :app/config)
                           :handler (ig/ref :app/handler)}
             :app/db {:config (ig/ref :app/config)}})

(defmethod ig/init-key :app/config [_ _]
  (edn/read-string (slurp (io/resource "config.edn"))))

(defmethod ig/init-key :app/handler [_ {:keys [db]}]
  (api/app-handler db))

(defmethod ig/init-key :app/adapter [_ {:keys [config handler]}]
  (adapter/app-server handler {:port (:port config)}))

(defmethod ig/halt-key! :app/adapter [_ server]
  (adapter/stop-server server))

(def system
  (atom nil))

(defn start-system! []
  (reset! system
          (ig/init config)))

(defn halt-system! []
  (swap! system
         (fn [s] (ig/halt! s))))

(comment
  (ig/halt! system)
  (ig/suspend! system)

  (ig/resume config system)
  (ig/halt! system [:app/adapter]))
