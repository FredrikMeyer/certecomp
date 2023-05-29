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
          [:li {:key (:id sess)} (str "Place: " (:place sess)
                                      ". Date: " (.toLocaleDateString (-> (* 1000 (:date sess)) js/Date.)))])]])

(defn exercises []
  [:div "exercises"
   [:ul
    (for [exc (:exercises @state)]
      [:li {:key (:id exc)}  (:name exc) (str " (goal reps: " (:goal-reps exc) ")")])]])

(defonce page-name (atom {:page-name "exercises"}))

(defn current-page []
  (js/console.log (:exercises @state))
  ;; (println @page-name)
  (fn []
    [:main
     [:h1 {:on-click #(js/console.log "hei")
           :style {:border-bottom "1px dotted black"}} "Certecomp"]
     [:p
      [:input {:type "button"
               :on-click (fn []
                           (swap! page-name (fn [m] (assoc-in m [:page-name] "exercises")))
                           (fetch-exercises))
               :value "Show all exercises"
               :style {:margin-right "5px"}}]
      [:input {:type "button"
               :on-click (fn []
                           (fetch-sessions)
                           (swap! page-name (fn [m] (assoc-in m [:page-name] "sessions"))))
               :value "Show all sessions"}]]
     (cond (= (:page-name @page-name) "exercises")
           [exercises]
           (= (:page-name @page-name) "sessions")
           [sessions]
           :else [:div "Unknown page."])]))

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
