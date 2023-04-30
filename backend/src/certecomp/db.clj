(ns certecomp.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [integrant.core :as ig]
            [taoensso.timbre :as log]
            [honey.sql :as hsql]
            [honey.sql.helpers :as h]))

(def ^:dynamic *config*
  {:db {:options {:dbtype "sqlite"
                  :dbname      "db/database.db"
                  :classname   "org.sqlite.JDBC"}}})

(defmethod ig/init-key :db [_ b2]
  (let [datasource (jdbc/get-datasource (:options b2))]
    (with-open [c (jdbc/get-connection datasource)]
      (log/info "Creating DB tables if they do not exist.")

      (jdbc/execute! c ["PRAGMA foreign_keys = ON"])
      (doseq [s [(hsql/format
                  (h/create-table :exercisetypes :if-not-exists
                                  (h/with-columns
                                    [:id :integer :primary-key]
                                    [:name :unique [:not nil]])))

                 (hsql/format
                  (h/create-table :exercise :if-not-exists
                                  (h/with-columns
                                    [:id :integer :primary-key]
                                    [:date :integer]
                                    [:type :integer]
                                    [[:foreign-key :type] [:references :exercisetypes :id]])))

                 (hsql/format
                  (h/create-table :sets :if-not-exists
                                  (h/with-columns
                                    [:id :integer :primary-key]
                                    [:reps :integer]
                                    [:goal-reps :integer]
                                    [:weight :float]
                                    [:exercise :integer]
                                    [[:foreign-key :exercise]
                                     [:references :exercise :id]])))]]
        (log/info "Running SQL: " s)
        (jdbc/execute! c s)))

    {:datasource (jdbc/get-datasource (:options b2))
     :get-connection (fn []
                       (let [c (jdbc/get-connection datasource)]
                         (jdbc/execute! c ["PRAGMA foreign_keys = ON"])
                         c))}))

(defmethod ig/halt-key! :db [_ b2]
  (log/info "Halted."))

(def system
  (ig/init *config*))

(defn get-connection []
  ((get-in system [:db :get-connection])))

(comment
  (ig/halt! system))

(defn create-set [exercise-id reps goal-reps weight]
  (let [res  (with-open [c (get-connection)]
               (sql/insert! c "sets"
                            {:exercise exercise-id :reps reps :goal-reps goal-reps :weight weight}
                            jdbc/snake-kebab-opts))]
    (-> res vals first)))

(defn list-sets [exercise-id]
  (with-open [c (get-connection)]
    (map (fn [set] {:reps (:sets/reps set) :goal-reps (:sets/goal_reps set) :weight (:sets/weight set)})
         (sql/query c ["select * from sets where exercise=?" exercise-id]))))

(defn create-exercise-type [name]
  (let [result (with-open [c (get-connection)]
                 (sql/insert! c "exercisetypes" {:name name}))]
    (-> result vals first)))

(defn delete-exercise-type [id]
  (with-open [c (get-connection)]
    (sql/delete! c "exercisetypes" {:id id})))

(defn get-exercise-types []
  (with-open [c (get-connection)]
    (let [res (sql/query c ["SELECT * FROM exercisetypes"])]
      (map (fn [r] {:id (:exercisetypes/id r) :name (:exercisetypes/name r)}) res))))

(defn create-exercise [type date]
  (with-open [c (get-connection)]
    (sql/insert! c "exercise" {:type type :date date})))

(defn get-exercises []
  (with-open [c (get-connection)]
    (let [exercises (sql/query c ["select * from exercise inner join exercisetypes on exercise.type=exercisetypes.id"])]
      (for [exercise exercises]
        {:id (:exercise/id exercise)
         :date (:exercise/date exercise)
         :type {:id (:exercise/type exercise)
                :name (:exercisetypes/name exercise)}
         :sets (list-sets (:exercise/id exercise))}))))

(comment

  ;; (jdbc/execute! db ["INSERT INTO testjson (name, stuff) values('gffgg', json('{\"hei\": 2}'))"])

  (create-exercise-type "knebøy")
  (create-exercise-type "markløft")
  (def db
    ^:private
    {:dbtype      "sqlite"
     :dbname      "db/database.db"
     :classname   "org.sqlite.JDBC"})
  (sql/query db ["select * from exercisetypes where id=?" 1])

  (sql/query db ["select * from exercisetypes inner join exercise on exercise.type"])

  (jdbc/execute! db ["select * from exercise"])

  (jdbc/execute! db ["drop table sets"])
  (jdbc/execute! db ["drop table exercise"])

  (jdbc/execute! db "PRAGMA foreign_keys = ON;")

  (jdbc/execute! db ["SELECT name FROM sqlite_schema WHERE type='table' ORDER BY name"])
  (jdbc/db-do-commands db (jdbc/drop-table-ddl :exercisetypes)))
