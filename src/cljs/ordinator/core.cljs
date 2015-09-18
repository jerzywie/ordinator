(ns ordinator.core
  (:require [ordinator.order :as order]
            [ordinator.login :as login]
            [ordinator.todo :as todo]
            [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType])
  (:import goog.History))


;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "Welcome to Ordinator"]
   [:div [:a {:href "#/order"} "View your order"]]
   [:div [:a {:href "#/about"} "go to about page"]]
   [:div [:a {:href "#/todo"} "go to to-do page"]]])

(defn login-page []
  [login/render-login-page])

(defn main-page []
  (if (login/logged-in?)
    (home-page)
    (login-page)))

(defn about-page []
  [:div [:h2 "About Ordinator"]
   [:div [:a {:href "#/"} "go to the home page"]]])

(defn order-page []
  [order/render-order-page])

(defn todo-page []
  (todo/todo-app))

(defn current-page []
  [:div [(session/get :current-page)]])

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
