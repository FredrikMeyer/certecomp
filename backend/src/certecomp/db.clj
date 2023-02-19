(ns certecomp.db
  (:require [clojure.java.jdbc :as jdbc]))

(def db
  ^:private
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "db/database.db"})

(defn create-exercise-type [name]
  (jdbc/insert! db :exercisetypes {:name name})
  )

(defn get-exercise-types []
  (jdbc/query db ["SELECT * FROM exercisetypes"]))

(comment 
    (jdbc/db-do-commands db
                       (jdbc/create-table-ddl :exercisetypes
                                              [[:id :integer :primary :key]
                                               [:name :unique :not :null]]))

  (jdbc/db-do-commands db
                       (jdbc/create-table-ddl :testjson
                                              [[:id :integer :primary :key]
                                               [:name :unique :not :null]
                                               [:stuff]]))

  (jdbc/execute! db ["INSERT INTO testjson (name, stuff) values('gffgg', json('{\"hei\": 2}'))"])
  
  (create-exercise-type "knebøy")
  (create-exercise-type "markløft")

  (jdbc/query db ["select * from exercisetypes"])
  (jdbc/query db ["select json_extract(testjson.stuff) from testjson"])

  (jdbc/execute! db (jdbc/drop-table-ddl :exercisetypes))

  (jdbc/db-do-commands db (jdbc/drop-table-ddl :exercisetypes)))
