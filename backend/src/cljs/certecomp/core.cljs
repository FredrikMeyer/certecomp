(ns certecomp.core
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]
   [goog.object :as gobj]
   [reagent.session :as session]
   [reitit.frontend :as reitit]))

(def state (atom {:exercises []}))

(defn current-page []
  (js/console.log (:exercises @state))
  (fn []
    [:main
     [:h1 {:on-click #(js/console.log "hei")} "Exercises"]
     [:div {:on-click (fn [] (-> (js/fetch "http://localhost:3000/api/exercise")
                                 (.then (fn [res] (.json res)))
                                 (.then (fn [res] (js/console.log res)
                                          (js/console.log (gobj/get res))
                                          (swap! state (fn [m] (assoc-in m [:exercises] (js->clj res :keywordize-keys true))))))))}
      "Fetch all exercises"]
     [:ul
      (for [exc (:exercises @state)]
        [:li {:key (:id exc)} (:name exc)])]]))

;; -------------------------
;; Initialize app

(defn mount-root []
  (rdom/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))

(defn ^:dev/after-load reload! []
  (mount-root))

;; example index.html for reagent
;; https://github.com/reagent-project/reagent/blob/master/site/public/index.html

;; reagent project template
;; https://github.com/reagent-project/reagent-template/tree/master/resources/leiningen/new/reagent

;; https://github.com/venantius/accountant
