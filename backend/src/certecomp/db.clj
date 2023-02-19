(ns certecomp.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [integrant.core :as ig]))

(def db
  ^:private
  {:dbtype "sqlite"
   :dbname      "db/database.db"
   :classname   "org.sqlite.JDBC"})

(def config
  {:db {:options {:dbtype "sqlite"
                  :dbname      "db/database.db"
                  :classname   "org.sqlite.JDBC"}}})

(defmethod ig/init-key :db [_ b2]
  (let [datasource (jdbc/get-datasource (:options b2))]
    (with-open [c (jdbc/get-connection datasource)]
            (jdbc/execute! c ["PRAGMA foreign_keys = ON"
                              ])
            (jdbc/execute! c [
                        "CREATE TABLE if not exists exercisetypes (id integer primary key, name unique not null)"
                              ])
                        (jdbc/execute! c [
                        "CREATE TABLE if not exists exercise (id integer primary key, type integer, foreign key(type) references exercisetypes(id))"
                                          ])
                  (jdbc/execute! c [
                                    "CREATE TABLE if not exists sets (id integer primary key, reps integer, goal_reps integer, weight float, exercise integer, foreign key(exercise) references exercise(id))"
                                    ])
      )
    {
   :datasource (jdbc/get-datasource (:options b2))
   :get-connection (fn []
                     (let [c (jdbc/get-connection datasource)]
                       (jdbc/execute! c ["PRAGMA foreign_keys = ON"])
                       c
                       ))}
    )
  )

(defmethod ig/halt-key! :db [_ b2]
  (println "Halted" b2))

(def system
  (ig/init config))

(defn get-connection []
  ((get-in system [:db :get-connection])) 
  )

(comment
  (ig/halt! system))

(defn create-set [exercise-id reps goal-reps weight]
  (with-open [c (get-connection)]
    (sql/insert! c "sets" {:exercise exercise-id :reps reps :goal-reps goal-reps :weight weight} jdbc/snake-kebab-opts)
    ))

(defn list-sets [exercise-id]
  (with-open [c (get-connection)]
    (map (fn [set] {:reps (:sets/reps set) :goal-reps (:sets/goal_reps set) :weight (:sets/weight set)})
         (sql/query c ["select * from sets where exercise=?" exercise-id]))
    ))


(defn create-exercise-type [name]
  (with-open [c (get-connection)]
    (sql/insert! c "exercisetypes" {:name name})
    ))

(defn get-exercise-types []
  (with-open [c (get-connection)]
    (let [res (sql/query c ["SELECT * FROM exercisetypes"])]
      (map (fn [r] {:id (:exercisetypes/id r) :name (:exercisetypes/name r)}) res))
    ))

(defn create-exercise [type]
  (with-open [c (get-connection)]
    (sql/insert! c "exercise" {:type type})
    )) 

(defn get-exercises []
  (with-open [c (get-connection)]
    (let [exercises (sql/query c ["select * from exercise"])]
      (for [exercise exercises]
        {:id (:exercise/id exercise)
         :sets         (list-sets (:exercise/id exercise))}
        
        ))
    )
  )


;; (try (create-exercise-type "knebøy") (catch Exception e (.toString (.getResultCode e))))

(comment
  (jdbc/db-do-commands db
                       (jdbc/create-table-ddl :exercisetypes
                                              [[:id :integer :primary :key]
                                               [:name :unique :not :null]]))

  (jdbc/db-do-commands db
                       [(jdbc/create-table-ddl :testjson
                                               [[:id :integer :primary :key]
                                                [:name :unique :not :null]
                                                [:stuff]] {:conditional? true})])

  (jdbc/execute! db ["INSERT INTO testjson (name, stuff) values('gffgg', json('{\"hei\": 2}'))"])

  (create-exercise-type "knebøy")
  (create-exercise-type "markløft")

  (jdbc/query db ["select * from exercisetypes"])
  (jdbc/query db ["select json_extract(testjson.stuff) from testjson"])

  (jdbc/execute! db ["drop table sets"])

  (jdbc/execute! db "PRAGMA foreign_keys = ON;")
  (jdbc/query db "PRAGMA foreign_keys")

  (jdbc/db-do-commands db (jdbc/drop-table-ddl :exercisetypes)))
