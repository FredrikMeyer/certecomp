(ns certecomp.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [ring.mock.request :as request]
            [certecomp.core :as core]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 1 1))))

(deftest first-test
  (testing "main route"
    (println (request/request :GET "/"))
    (let [response (core/app (request/request :get "/api/types"))]
      (is (= 200 (:status response))))))
