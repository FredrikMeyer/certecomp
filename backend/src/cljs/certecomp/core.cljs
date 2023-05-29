(ns certecomp.core
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]
   [goog.object :as gobj]
   ["react" :as react]
   [reagent.dom.client :as rdomc]
   [reagent.session :as session]
   [reitit.frontend :as reitit]))

;; State

(defonce state (atom {:exercises []
                      :sessions []}))

;; Fetchers
(defn fetch-exercises []
  (-> (js/fetch "http://localhost:3000/api/exercise")
      (.then (fn [res] (.json res)))
      (.then (fn [res]
               (js/console.log res)
               (let [clj-obj (js->clj res :keywordize-keys true)]
                 (swap! state (fn [m] (assoc-in m [:exercises] clj-obj))))))))

(defn fetch-sessions []
  (-> (js/fetch "http://localhost:3000/api/session")
      (.then (fn [res] (.json res)))
      (.then (fn [res]
               (js/console.log res)
               (let [clj-obj (js->clj res :keywordize-keys true)]
                 (swap! state (fn [m] (assoc-in m [:sessions] clj-obj))))))))

;; Components

;; (fetch-exercises)

(defn sessions []
  (println @state)
  [:div "sessions"
   [:ul (for [sess (:sessions @state)]
          [:li {:key (:id sess)} (str "Place: " (:place sess) ". Date: " (.toLocaleDateString (-> (* 1000 (:date sess)) js/Date.)))])]])

(defn current-page []
  (js/console.log (:exercises @state))

  (let [current-page (atom :exercises)]
    (fn []
      [:main
       [:h1 {:on-click #(js/console.log "hei")} "Exercises"]
       [:p
        [:input {:type "button"
                 :on-click fetch-exercises
                 :value "Show all exercises"
                 :style {:margin-right "5px"}}]
        [:input {:type "button"
                 :on-click (fn []
                             (fetch-sessions)
                             (reset! current-page :sessions))
                 :value "Show all sessions"}]]
       (cond (= @current-page :exercises)
             [:ul
              (for [exc (:exercises @state)]
                [:li {:key (:id exc)}  (:name exc) (str " (goal reps: " (:goal-reps exc) ")")])]
             (= @current-page :sessions)
             [sessions])])))

;; -------------------------
;; Initialize app

(defonce div-root (.getElementById js/document "app"))
(defonce react-root (rdomc/create-root div-root))

(defn mount-root []
  (let [react-root react-root]
    (rdomc/render react-root [:f> current-page])))

(defn init! []
  (mount-root))

(defn ^:dev/after-load reload! []
  (mount-root))

;; example index.html for reagent
;; https://github.com/reagent-project/reagent/blob/master/site/public/index.html

;; reagent project template
;; https://github.com/reagent-project/reagent-template/tree/master/resources/leiningen/new/reagent

;; https://github.com/venantius/accountant
