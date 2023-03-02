(ns certecomp.core
  (:require
   [certecomp.db :as db]
   [clojure.spec.alpha :as s]
   [muuntaja.core :as m]
   [reitit.coercion.spec]
   [reitit.core :as r]
   [reitit.ring :as ring]
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [camel-snake-kebab.core :as csk]
   [reitit.ring.coercion :as rrc]
   [reitit.ring.middleware.exception :as exception]
   [reitit.ring.middleware.muuntaja :as rrmm]
   [ring.adapter.jetty :as jetty]
   [ring.util.response :as resp]))

(s/def ::exercise-id int?)
(s/def ::get-set-path-params (s/keys :req-un [::exercise-id]))

(s/def ::id int?)
(s/def ::delete-type-params (s/keys :req-un [::id]))
(s/def ::string string?)

(def router
  (ring/router ["/api"
                [""
                 ["/docs/*" {:no-doc true
                             :get (swagger-ui/create-swagger-ui-handler {:url "/api/swagger.json"})}]
                 ["/swagger.json" {:get (swagger/create-swagger-handler)}]
                 ]
                ["/types"
                 ["" {:get {:summary "List exercise types."
                            :handler (fn [_]
                                       (resp/response (db/get-exercise-types)))}
                      :post {:summary "Create a new exercise type."
                             :parameters {:body {:name ::string}}
                             :handler (fn [{:keys [body-params] :as request}]
                                        (println body-params)
                                        (db/create-exercise-type (:name body-params))
                                        (resp/response "ok"))}}]
                 ["/:id" {   :parameters {:path ::delete-type-params}
                          :delete {:summary "Delete an exercise type."
                                   :handler (fn [{path-params :path-params}]
                                              (println path-params)
                                              (resp/response "hei"))}}]]
                ["/set"
                 ["" {
                      :post {:summary "Create a new set."
                             :parameters {:body {:exercise-id ::exercise-id}}
                             :handler (fn [r]
                                               ;; (println body-params)
                                               ;; (db/create-set 0 0 5 50)
                                        (resp/response "dddddd"))}
                      :get {:handler (fn [r] (resp/response "jadda"))}}]
                 ["/:exercise-id" {:parameters {:path ::get-set-path-params}
                                   :get {:handler (fn [{path-params :path-params}]
                                                    (let [exercise-id (:exercise-id path-params)]
                                                      (resp/response (db/list-sets exercise-id))))}}]]
                ["/exercise" {:summary "Get all registered exercises."
                              :get {:handler (fn [request]
                                               (resp/response (db/get-exercises)))}}]]

               {:data {:muuntaja (m/create
                                  (assoc-in m/default-options
                                            [:formats "application/json" :encoder-opts]
                                            {:encode-key-fn csk/->camelCaseString}))
                       :coercion reitit.coercion.spec/coercion
                       :middleware [rrmm/format-middleware
                                    ;; exception/exception-middleware
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
          (assoc-in [:headers "Access-Control-Allow-Headers"] "content-type")))))

(def app-handler
  (-> #'app
      (add-cors)
      ;; (wrap-content-type "application/json")
      ))

(defn app-server-start []
  (reset! app-server-instance (jetty/run-jetty #'app-handler
                                               {:port 3000 :join? false})))

(defn app-server-stop
  []
  (when @app-server-instance
    (.stop @app-server-instance))
  (reset! app-server-instance nil))
