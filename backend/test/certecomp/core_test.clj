(ns certecomp.core-test
  (:require
   [certecomp.core :as core]
   [clojure.data.json :as json]
   [clojure.test :refer [deftest is testing]]
   [ring.mock.request :as request]))

(deftest first-test
  (testing "main route"
    (println (request/request :GET "/"))
    (let [response (core/app (request/request :get "/api/types"))]
      (is (= 200 (:status response))))))

(deftest get-exercises
  (testing "exerciss"
    (let [resp (core/app (request/request :get "/api/exercise"))
          body  (slurp (:body resp))
          asjson (json/read-str body)]
      (println body)
      (println asjson)
      (is (vector? asjson)))))

(deftest create-delete-type
  (let [name (str (random-uuid))]
    (testing "create-delete-type"
      (do
        ;; Create a type
        (let [resp (core/app (->
                              (request/request :post "/api/types")
                              (request/json-body {:name name})))]
          (println resp)
          (is (vector? [])))
        ;; Get it
        (let [resp (core/app (->
                              (request/request :get "/api/types")))
              response-read (-> resp :body slurp json/read-str)
              filtered (filter (fn [t] (= (get t "name") name)) response-read)]
          (println response-read)
          (is (= 1 (count filtered)))

          (let [type-id (get (first filtered) "id")]
            ;; Delete it
            (let [resp (core/app (->
                                  (request/request :delete (str "/api/types/" type-id))))]
              (println resp)
              (is (= 200 (:status resp))))
            (let [resp (core/app (->
                                  (request/request :get "/api/types")))
                  response-read (-> resp :body slurp json/read-str)
                  filtered (filter (fn [t] (= (get t "name") name)) response-read)]
              (println resp)
              (is (= 200 (:status resp)))
              (is (= 0 (count filtered))))))))))
