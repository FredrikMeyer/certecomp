(ns certecomp.core
  (:require
   [certecomp.db :as db]
   [muuntaja.core :as m]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as rrc]
   [reitit.ring.middleware.exception :as exception]
   [reitit.ring.middleware.muuntaja :as rrmm]
   [ring.util.response :as resp]
   [ring.adapter.jetty :as jetty]))

(def app
  (ring/ring-handler
   (ring/router ["/api"
                 ["/types" {:get {:handler (fn [request]
                                             (resp/response (db/get-exercise-types)))}
                            :post {:handler (fn [{:keys [body-params] :as request}]
                                              (println body-params)
                                              (db/create-exercise-type (:name body-params))
                                              (resp/response "ok"))}}]
                 ["/set" {:get {:handler (fn [request]
                                           (resp/response []))}}]]

                {:data {:muuntaja m/instance
                        :middleware [rrmm/format-middleware
                                     exception/exception-middleware
                                     rrc/coerce-response-middleware
                                     rrc/coerce-request-middleware]}})

   (constantly {:status 404 :body "Unknown path."})))

(defonce app-server-instance (atom nil))

(defn wrap-content-type [handler content-type]
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers "Content-Type"] content-type))))

(defn add-cors [handler]
  (fn [request]
    (let [response (handler request)]
      (-> response
          (assoc-in [:headers "Access-Control-Allow-Origin"] "http://127.0.0.1:5173")
          (assoc-in [:headers "Access-Control-Allow-Headers"] "content-type")))))

(def app-handler
  (-> #'app
      (add-cors)
      ;; (wrap-content-type "application/json")
      ))

(defn app-server-start []
  (reset! app-server-instance (jetty/run-jetty #'app-handler
                                               {:port 3000 :join? false})))

(defn app-server-stop
  []
  (when @app-server-instance
    (.stop @app-server-instance))
  (reset! app-server-instance nil))

;; (jdbc/db-do-commands db ["SELECT * FRM ff"])

;; (defn create-db
;;   "create db and table"
;;   []
;;   (try (jdbc/db-do-commands db
;;                             (jdbc/create-table-ddl :exercises
;;                                                    [[:timestamp :datetime :default :current_timestamp]
;;                                                     [:url :text]
;;                                                     [:title :text]
;;                                                     [:body :text]]))
;;        (catch Exception e
;;          (println (.getMessage e)))))

;; (create-db)

;; (jdbc/db-do-commands db ["SELECT * FROM news"])

(comment
  (jdbc/db-do-commands db
                       (jdbc/create-table-ddl :mytable [[:name "varchar(32)" :primary :key]]))

  (jdbc/db-do-commands db
                       (jdbc/query db ["SELECT * FROM mytable"]))
  (jdbc/query db ["SELECT * FROM mytable"])

  (jdbc/query db ["INSERT INTO mytable (name) values (?)" "hei"])

  (jdbc/insert! db :mytable {:name "heddsdsdu"})

  (jdbc/query db [])

  (jdbc/query db ["select * from sqlite_master where type='table'"])
  ({:type "table", :name "news", :tbl_name "news", :rootpage 2, :sql "CREATE TABLE news (timestamp datetime default current_timestamp, url text, title text, body text)"} {:type "table", :name "mytable", :tbl_name "mytable", :rootpage 3, :sql "CREATE TABLE mytable (name varchar(32) primary key)"} {:type "table", :name "user2", :tbl_name "user2", :rootpage 5, :sql "CREATE TABLE user2(id integer primary key, name text, interests json)"}))
