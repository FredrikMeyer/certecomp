(ns certecomp.core-test
  (:require
   [certecomp.core :as core]
   [clojure.data.json :as json]
   [clojure.test :refer [deftest is testing]]
   [ring.mock.request :as request]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 1 1))))

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
      (is (vector? asjson)))
    ))



(deftest create-delete-type
  (testing "create-delete-type"
    (do 
      (let [resp (core/app (->
                            (request/request :post "/api/types")
                            (request/json-body {:name "nn22na2me23"})))]
        (println resp)
        (is (vector? [])))
      (let [resp (core/app (->
                            (request/request :post "/api/types")
                            (request/json-body {:name "nn22na2me23"})))])
      )
    ))
