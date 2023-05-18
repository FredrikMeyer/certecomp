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
(s/def ::session-id int?)
(s/def ::workout-id (s/and int? (comp not neg-int?)))
(s/def ::workout-id-param (s/keys :req-un [::workout-id]))

(s/def ::id int?)
(s/def ::delete-type-params (s/keys :req-un [::id]))
(s/def ::string string?)
(s/def ::float-or-int (s/or :float float? :int int?))
(s/def ::date int?)
(s/def ::name string?)
(s/def ::description string?)
(s/def ::reps (s/and int? pos?))
(s/def ::goal-reps (s/and int? pos?))
(s/def ::goal-number-of-sets int?)
(s/def ::exercise-type (s/keys :req-un [::id ::name]))

(s/def ::exercise (s/keys :req-un [::id ::name ::description ::goal-reps ::goal-number-of-sets]))
(s/def ::new-exercise (s/keys :req-un [::name] :opt-un [::description ::goal-reps ::goal-number-of-sets]))

(s/def ::place string?)
(s/def ::shape string?)
(s/def ::session (s/keys :req-un [::id ::date ::place ::shape]))
(s/def ::new-session (s/keys :req-un [::date ::place ::shape]))
(s/def ::shape string?)
(s/def ::workout (s/keys :req-un [::date] :opt-un [::place ::shape]))

(s/def ::weight (s/and float? pos?))
(s/def ::set (s/keys :req-un [::id ::reps ::weight ::workout-id]))

(s/def ::new-workout (s/keys :req-un [::session-id ::exercise-id]))
(s/def ::workout (s/keys :req-un [::id ::session-id ::exercise-id]))

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
                 ["/swagger.json" {:no-doc true
                                   :get (swagger/create-swagger-handler)}]]
                ["/exercise"
                 {:swagger {:tags ["exercise"]}}
                 ["" {:get {:summary "List exercise types."
                            :responses {200 {:body (s/coll-of ::exercise)}}
                            :handler (fn [_]
                                       (resp/response (db/get-exercises db)))}
                      :post {:summary "Create a new exercise type."
                             :parameters {:body ::new-exercise}
                             :handler (fn [{:keys [body-params]}]
                                        (println body-params)
                                        (let [res (db/create-exercise db body-params)]
                                          (resp/response res)))}}]
                 ["/:id" {:parameters {:path ::delete-type-params}
                          :delete {:summary "Delete an exercise type."
                                   :handler (fn [{path-params :path-params}]
                                              (t/info "Deleting. Params: " path-params)
                                              (let [id (:id path-params)]
                                                (db/delete-exercise db id))
                                              (resp/status 200))}}]]
                ["/session"
                 {:swagger {:tags ["session"]}}
                 ["" {:get {:summary "List all sessions."
                            :responses {200 {:body (s/coll-of ::session)}}
                            :handler (fn [_]
                                       (let [res (db/get-sessions db)]
                                         (resp/response res)))}
                      :post {:summary "Register a new session."
                             :parameters {:body ::new-session}
                             :handler (fn [{:keys [body-params]}]
                                        (let [resp (db/create-session db body-params)]
                                          (resp/status 200)))}}]]

                ["/set"
                 {:swagger {:tags ["set"]}}
                 ["" {:post {:summary "Create a new set."
                             :parameters {:body {:workout-id ::workout-id
                                                 :reps int?
                                                 :weight ::float-or-int}}
                             :handler (fn [{:keys [body-params]}]
                                        (t/info "Creating new set. Body params:" body-params)
                                        (let [reps (:reps body-params)
                                              workout-id (:workout-id body-params)
                                              weight (:weight body-params)]
                                          (-> (db/create-set db workout-id reps weight)
                                              resp/response)))}}]
                 ["/:workout-id" {:parameters {:path ::workout-id-param}
                                  :summary "Get sets by workout ID."
                                  :responses {200 {:body ::set}}
                                  :get {:handler (fn [{path-params :path-params}]
                                                   (let [workout-id (:workout-id path-params)]
                                                     (resp/response (db/list-sets db workout-id))))}}]]
                ["/workout" {:swagger {:tags ["workout"]}
                             :get {:summary "Get all registered workouts."
                                   :responses {200 {:body ::workout}}
                                   :handler (fn [_]
                                              (resp/response (db/get-workouts db)))}
                             :post {:summary "Register a new workout."
                                    :parameters {:body ::new-workout}
                                    :handler (fn [{:keys [body-params]}]
                                               (db/create-workout db body-params)
                                               (resp/response {:msg "Registered workout."}))}}]]

               {:data {:muuntaja (m/create
                                  (-> m/default-options
                                      ;; (assoc-in [:formats "application/json" :encoder-opts]
                                      ;;           {:encode-key-fn csk/->camelCaseString})
                                      ))

                       :coercion reitit.coercion.spec/coercion
                       :middleware [rrmm/format-middleware ;; content negotiation
                                    exception-middleware
                                    ;; coercing response bodys
                                    rrc/coerce-response-middleware
                                    ;; coercing request parameters
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
