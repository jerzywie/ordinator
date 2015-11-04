(ns ordinator.core
  (:require [ordinator.order :as order]
            [ordinator.login :as login]
            [ordinator.collation :as col]
            [ordinator.todo :as todo]
            [ordinator.utils :as utils]
            [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType])
  (:import goog.History))


;; -------------------------
;; Views

(defn home-page []
  (let [username (utils/get-username)]
    [:div
     [login/header]
     [:h2 "Welcome to Ordinator " username]]))

(defn login-page []
  [login/render-login-page])

(defn main-page []
  (home-page))

(defn about-page []
  [:div
   [login/header]
   [:h2 "About Ordinator"]])

(defn order-page []
  [order/render-order-page])

(defn allorders-page []
  [col/render-allorders-page])

(defn todo-page []
  (todo/todo-app))

(defn current-page []
  [:div [(if (utils/logged-in?)
           (session/get :current-page)
           #'login-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'main-page))

(secretary/defroute "/login" []
  (session/put! :current-page #'login-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

(secretary/defroute "/order" []
  (session/put! :current-page #'order-page))

(secretary/defroute "/allorders" []
  (session/put! :current-page #'allorders-page))

(secretary/defroute "/todo" []
  (session/put! :current-page #'todo-page))
;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
