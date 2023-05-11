(ns certecomp.api
  (:require
   [certecomp.db :as db]
   [clojure.spec.alpha :as s]
   [muuntaja.core :as m]
   [reitit.coercion.spec]
   [taoensso.timbre :as t]
   [reitit.ring :as ring]
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [camel-snake-kebab.core :as csk]
   [reitit.ring.coercion :as rrc]
   [reitit.ring.middleware.exception :as exception]
   [reitit.ring.middleware.muuntaja :as rrmm]
   [ring.util.response :as resp]
   [taoensso.timbre :as timbre :refer [error]]))

(s/def ::exercise-id int?)
(s/def ::get-set-path-params (s/keys :req-un [::exercise-id]))

(s/def ::id int?)
(s/def ::delete-type-params (s/keys :req-un [::id]))
(s/def ::string string?)
(s/def ::float-or-int (s/or :float float? :int int?))
(s/def ::date int?)
(s/def ::name string?)
(s/def ::exercise-type (s/keys :req [::id ::name]))

(s/def ::exercise (s/keys :req [::id ::date]))

(def exception-middleware
  (exception/create-exception-middleware
   (merge
    exception/default-handlers
    {::exception/wrap (fn [handler e request]
                        (error e "Error occured in endpoint. Request: " (:parameters request))
                        (handler e request))})))

(defn router [db]
  (ring/router ["/api"
                [""
                 ["/docs/*" {:no-doc true
                             :get (swagger-ui/create-swagger-ui-handler {:url "/api/swagger.json"})}]
                 ["/swagger.json" {:get (swagger/create-swagger-handler)}]]
                ["/types"
                 {:swagger {:tags ["types"]}}
                 ["" {:get {:summary "List exercise types."
                            :handler (fn [_]
                                       (resp/response (db/get-exercise-types db)))}
                      :post {:summary "Create a new exercise type."
                             :parameters {:body {:name ::string}}
                             :handler (fn [{:keys [body-params]}]
                                        (println body-params)
                                        (let [name (:name body-params)
                                              res (db/create-exercise-type db name)]
                                          (resp/response res)))}}]
                 ["/:id" {:parameters {:path ::delete-type-params}
                          :delete {:summary "Delete an exercise type."
                                   :handler (fn [{path-params :path-params}]
                                              (println "hei" path-params)
                                              (let [id (:id path-params)]
                                                (db/delete-exercise-type db id))
                                              (resp/response "ok"))}}]]
                ["/set"
                 {:swagger {:tags ["set"]}}
                 ["" {:post {:summary "Create a new set."
                             :parameters {:body {:exercise-id ::exercise-id
                                                 :reps int?
                                                 :reps-goal int?
                                                 :weight ::float-or-int}}
                             :handler (fn [{:keys [body-params]}]
                                        (println body-params)
                                        (let [reps (:reps body-params)
                                              reps-goal (:reps-goal body-params)
                                              exercise-id (:exercise-id body-params)
                                              weight (:weight body-params)]
                                          (-> (db/create-set db exercise-id reps reps-goal weight)
                                              resp/response)))}
                      :get {:handler (fn [r] (resp/response "jadda"))}}]
                 ["/:exercise-id" {:parameters {:path ::get-set-path-params}
                                   :summary "Get sets by exercise ID."
                                   :get {:handler (fn [{path-params :path-params}]
                                                    (let [exercise-id (:exercise-id path-params)]
                                                      (resp/response (db/list-sets db exercise-id))))}}]]
                ["/exercise" {:swagger {:tags ["exercises"]}
                              :get {:summary "Get all registered exercises."
                                    ;; :responses {200 {:body ::exercise}}
                                    :handler (fn [request]
                                               (resp/response (db/get-exercises db)))}
                              :post {:summary "Register a new exercise."
                                     :parameters {:body {:type ::id :date ::date}}
                                     :handler (fn [{:keys [body-params]}]
                                                (let [type (:type body-params)
                                                      date (:date body-params)]
                                                  (db/create-exercise db type date)
                                                  (resp/response {:msg "Created exercise."})))}}]]

               {:data {:muuntaja (m/create
                                  (assoc-in m/default-options
                                            [:formats "application/json" :encoder-opts]
                                            {:encode-key-fn csk/->camelCaseString}))
                       :coercion reitit.coercion.spec/coercion
                       :middleware [rrmm/format-middleware
                                    exception-middleware
                                    rrc/coerce-response-middleware
                                    rrc/coerce-request-middleware]}}))

(defn app [db-config]
  (ring/ring-handler
   (router db-config)
   (constantly {:status 404 :body "Unknown path."})))

(defn add-cors [handler]
  (fn [request]
    (let [response (handler request)]
      (-> response
          (assoc-in [:headers "Access-Control-Allow-Origin"] "http://127.0.0.1:5173")
          (assoc-in [:headers "Access-Control-Allow-METHODS"] "GET,POST,DELETE")
          (assoc-in [:headers "Access-Control-Allow-Headers"] "content-type")))))

(defn app-handler [db-config]
  (-> (app db-config)
      (add-cors)))
