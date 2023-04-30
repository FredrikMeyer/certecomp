(ns certecomp.core
  (:require
   [certecomp.db :as db]
   [clojure.spec.alpha :as s]
   [muuntaja.core :as m]
   [reitit.coercion.spec]
   [reitit.core :as r]
   [taoensso.timbre :as t]
   [reitit.ring :as ring]
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [camel-snake-kebab.core :as csk]
   [reitit.ring.coercion :as rrc]
   [reitit.ring.middleware.exception :as exception]
   [reitit.ring.middleware.muuntaja :as rrmm]
   [ring.adapter.jetty :as jetty]
   [ring.util.response :as resp]
   [taoensso.timbre :as timbre :refer [log info error]]))

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

(def router
  (ring/router ["/api"
                [""
                 ["/docs/*" {:no-doc true
                             :get (swagger-ui/create-swagger-ui-handler {:url "/api/swagger.json"})}]
                 ["/swagger.json" {:get (swagger/create-swagger-handler)}]]
                ["/types"
                 {:swagger {:tags ["types"]}}
                 ["" {:get {:summary "List exercise types."
                            ;; :responses {200 {:body (s/coll-of ::exercise-type)}}
                            :handler (fn [_]
                                       (resp/response (db/get-exercise-types)))}
                      :post {:summary "Create a new exercise type."
                             :parameters {:body {:name ::string}}
                             :handler (fn [{:keys [body-params]}]
                                        (println body-params)
                                        (let [name (:name body-params)
                                              res (db/create-exercise-type name)]
                                          (resp/response res)))}}]
                 ["/:id" {:parameters {:path ::delete-type-params}
                          :delete {:summary "Delete an exercise type."
                                   :handler (fn [{path-params :path-params}]
                                              (println "hei" path-params)
                                              (let [id (:id path-params)]
                                                (db/delete-exercise-type id))
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
                                          (-> (db/create-set exercise-id reps reps-goal weight)
                                              resp/response)))}
                      :get {:handler (fn [r] (resp/response "jadda"))}}]
                 ["/:exercise-id" {:parameters {:path ::get-set-path-params}
                                   :summary "Get sets by exercise ID."
                                   :get {:handler (fn [{path-params :path-params}]
                                                    (let [exercise-id (:exercise-id path-params)]
                                                      (resp/response (db/list-sets exercise-id))))}}]]
                ["/exercise" {:swagger {:tags ["exercises"]}
                              :get {:summary "Get all registered exercises."
                                    ;; :responses {200 {:body ::exercise}}
                                    :handler (fn [request]
                                               (resp/response (db/get-exercises)))}
                              :post {:summary "Register a new exercise."
                                     :parameters {:body {:type ::id :date ::date}}
                                     :handler (fn [{:keys [body-params]}]
                                                (let [type (:type body-params)
                                                      date (:date body-params)]
                                                  (db/create-exercise type date)
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

(def app
  (ring/ring-handler
   router
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
          (assoc-in [:headers "Access-Control-Allow-METHODS"] "GET,POST,DELETE")
          (assoc-in [:headers "Access-Control-Allow-Headers"] "content-type")))))

(def app-handler
  (-> #'app
      (add-cors)
      ;; (wrap-content-type "application/json")
      ))

(defn app-server-start []
  (reset! app-server-instance
          (jetty/run-jetty #'app-handler
                           {:port 3000 :join? false})))

(defn app-server-stop
  []
  (when @app-server-instance
    (.stop @app-server-instance))
  (reset! app-server-instance nil))

(defn -main [& args]
  (info "Starting server.")
  (app-server-start))
