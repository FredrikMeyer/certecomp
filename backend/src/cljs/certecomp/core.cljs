(ns certecomp.core
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]
   [reagent.session :as session]
   [reitit.frontend :as reitit]))

(defn current-page []
  (fn []
    [:main
     [:h1 {:on-click #(js/console.log "hei")} "Hei"]]))



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
