(ns certecomp.core-test
  (:require
   [certecomp.system :as system]
   [clojure.data.json :as json]
   [clojure.test :refer [deftest is testing use-fixtures]]
   [ring.mock.request :as request]))

(defn db-fixture [f] (system/start-system!)
  (f)
  (system/halt-system!))

(use-fixtures :once db-fixture)

(deftest first-test
  (testing "main route"
    (println (request/request :GET "/"))
    (let [handler (:app/handler @system/system)
          response (handler (request/request :get "/api/exercise"))]
      (is (= 200 (:status response))))))

(deftest get-exercises
  (testing "exercises"
    (let [handler (:app/handler @system/system)
          resp (handler (request/request :get "/api/exercise"))
          body  (slurp (:body resp))
          asjson (json/read-str body)]
      (println body)
      (println asjson)
      (is (vector? asjson)))))

(deftest create-delete-type
  (let [name (str (random-uuid))
        handler (:app/handler @system/system)]
    (testing "create-delete-exercise-type"
      (do
        ;; Create a new exercise
        (let [resp (handler (->
                             (request/request :post "/api/exercise")
                             (request/json-body {:name name :description "" :goal-reps 5 :goal-number-of-sets 5})))]
          (println resp)
          (is (vector? [])))
        ;; Get it back
        (let [resp (handler (->
                             (request/request :get "/api/exercise")))
              response-read (-> resp :body slurp json/read-str)
              filtered (filter (fn [t] (= (get t "name") name)) response-read)]
          (println response-read)
          (is (= 1 (count filtered)))

          (let [type-id (get (first filtered) "id")]
            ;; Delete it
            (let [resp (handler (->
                                 (request/request :delete (str "/api/exercise/" type-id))))]
              (println resp)
              (is (= 200 (:status resp))))
            (let [resp (handler (->
                                 (request/request :get "/api/exercise")))
                  response-read (-> resp :body slurp json/read-str)
                  filtered (filter (fn [t] (= (get t "name") name)) response-read)]
              (println resp)
              (is (= 200 (:status resp)))
              (is (= 0 (count filtered))))))))))
