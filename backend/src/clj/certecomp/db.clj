(ns certecomp.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [next.jdbc.result-set :as rs]
            [integrant.core :as ig]
            [taoensso.timbre :as log]
            [honey.sql :as hsql]
            [honey.sql.helpers :as h]))

(defmethod ig/init-key :app/db [_ {:keys [config]}]
  (let [db-config (:db config)
        datasource (jdbc/get-datasource db-config)]
    (with-open [c (jdbc/get-connection datasource)]
      (log/info "Creating DB tables if they do not exist.")

      (jdbc/execute! c ["PRAGMA foreign_keys = ON"])
      (doseq [s [(h/create-table :exercise :if-not-exists
                                 (h/with-columns
                                   [:id :integer :primary-key]
                                   [:name  :str [:not nil]]
                                   [:description :str]
                                   [:goal-reps :integer]
                                   [:goal-number-of-sets :integer]))

                 (h/create-table :session :if-not-exists
                                 (h/with-columns
                                   [:id :integer :primary-key]
                                   [:date :int]
                                   [:place :str]
                                   [:shape :str]))

                 (h/create-table :workout :if-not-exists
                                 (h/with-columns
                                   [:id :integer :primary-key]
                                   [:exercise-id :int]
                                   [:session-id :int]
                                   [[:foreign-key :exercise-id] [:references :exercise :id]]
                                   [[:foreign-key :session-id] [:references :session :id] :on-delete :cascade]))

                 (h/create-table :sets :if-not-exists
                                 (h/with-columns
                                   [:id :integer :primary-key]
                                   [:reps :int [:not nil]]
                                   [:weight :float]
                                   [:workout-id :int]
                                   [[:foreign-key :workout-id] [:references :workout :id] :on-delete :cascade]))]]

        (log/info "Running SQL: " s)
        (jdbc/execute! c (hsql/format s))))

    {:get-connection (fn []
                       (let [c (jdbc/get-connection datasource)]
                         (jdbc/execute! c ["PRAGMA foreign_keys = ON"])
                         c))}))

(def jdbc-opts (-> {}
                   (merge jdbc/snake-kebab-opts)
                   (assoc :return-keys {:return-keys true})))

(defn create-set [{:keys [get-connection]} {:keys [workout-id reps weight]}]
  (let [res (with-open [c (get-connection)]
              (jdbc/execute! c (-> (h/insert-into :sets)
                                   (h/columns :reps :weight :workout-id)
                                   (h/values [[reps weight workout-id]])
                                   hsql/format)
                             jdbc-opts))]
    res))

(defn list-sets [{:keys [get-connection]} workout-id]
  (with-open [c (get-connection)]

    (map (fn [set] {:reps (:sets/reps set) :goal-reps (:sets/goal_reps set) :weight (:sets/weight set)})
         (jdbc/execute! c (-> (h/select :*)
                              (h/from :sets)
                              (h/where [:= :workout-id workout-id])
                              hsql/format) jdbc-opts))))

(defn delete-set [{:keys [get-connection]} id]
  (with-open [c (get-connection)]
    (jdbc/execute! c (-> (h/delete-from :sets)
                         (h/where [:= :id id])
                         hsql/format))))

(defn create-exercise [{:keys [get-connection]}
                       {:keys [name description goal-reps goal-number-of-sets]}]
  (let [result (with-open [c (get-connection)]
                 (log/info "Inserting values name description goal-reps goal-number-of-sets." name description goal-reps goal-number-of-sets)
                 (jdbc/execute! c (-> (h/insert-into :exercise)
                                      (h/columns :name :description :goal-reps :goal-number-of-sets)
                                      (h/values [[name description goal-reps goal-number-of-sets]])
                                      hsql/format)
                                jdbc-opts))]
    result))

(defn delete-exercise [{:keys [get-connection]} id]
  (with-open [c (get-connection)]
    (jdbc/execute! c (-> (h/delete-from :exercise)
                         (h/where [:= :id id])
                         hsql/format) jdbc-opts)))

(defn get-exercises [{:keys [get-connection]}]
  (with-open [c (get-connection)]
    (->> (-> (h/select :*)
             (h/from :exercise)
             hsql/format)
         ((fn [q] (jdbc/execute! c q jdbc-opts)))
         (map (fn [r]
                (update-keys r (fn [k] (-> k name keyword))))))))

(defn create-session [{:keys [get-connection]} {:keys [date place shape]}]
  (with-open [c (get-connection)]
    (jdbc/execute! c (-> (h/insert-into :session)
                         (h/columns :date :place :shape)
                         (h/values [[date place shape]])
                         hsql/format) jdbc-opts)))

(defn get-sessions [{:keys [get-connection]}]
  (with-open [c (get-connection)]
    (->>
     (jdbc/execute! c (-> (h/select :*)
                          (h/from :session)
                          hsql/format))
     (map (fn [r] (update-keys r (fn [k] (-> k name keyword))))))))

(defn delete-session [{:keys [get-connection]} {:keys [session-id]}]
  (with-open [c (get-connection)]
    (jdbc/execute! c (-> (h/delete-from :session)
                         (h/where [:= :id session-id])
                         hsql/format) jdbc-opts)))

(defn create-workout [{:keys [get-connection]} {:keys [session-id exercise-id]}]
  (with-open [c (get-connection)]
    (let [res (jdbc/execute! c (-> (h/insert-into :workout)
                                   (h/columns :session-id :exercise-id)
                                   (h/values [[session-id exercise-id]])
                                   hsql/format)
                             jdbc-opts)]
      res)))

(defn get-workouts [{:keys [get-connection]}]
  (with-open [c (get-connection)]
    (let [query (-> (h/select :*)
                    (h/from :workout)
                    (hsql/format))]
      (map (fn [r] (update-keys r (fn [k] (-> k name keyword))))
           (jdbc/execute! c query jdbc-opts)))))

(comment
  (defn get-connection []
    (jdbc/get-connection

     {:dbtype      "sqlite"
      :dbname      "db/database.db"
      :classname   "org.sqlite.JDBC"}))

  (require '[certecomp.utils :as u])

  (let [c (fn [] (get-connection))
        db {:get-connection c}]
    (get-sessions db))

  (let [c (fn [] (get-connection))
        db {:get-connection c}]
    (create-session db {:date (u/get-current-time) :place "Myrens" :shape "ok"}))
  (let [c (fn [] (get-connection))
        db {:get-connection c}]
    ;; (create-session db {:date (u/get-current-time) :place "Myrens" :shape "good"})
    (create-workout db {:session-id 4 :exercise-id 2}))
  (let [c (fn [] (get-connection))
        db {:get-connection c}]
    (create-set db {:workout-id 1 :reps 5 :weight 50}))

  (let [c (fn [] (get-connection))
        db {:get-connection c}]
    (get-workouts db)))
