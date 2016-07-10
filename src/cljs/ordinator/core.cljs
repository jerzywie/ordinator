(ns cljs.ordinator.core
  (:require [petrol.core :as petrol]
            [petrol.routing :as petrol-routing]
            [cljs.ordinator.routes :as routes]
            [cljs.ordinator.view :as view]
            [ordinator.order :as order]
            [ordinator.login :as login]
            [ordinator.collation :as col]
            [ordinator.todo :as todo]
            [ordinator.utils :as utils]
            [reagent.core :as reagent :refer [atom]]
            [goog.events :as events]
            [goog.history.EventType :as EventType])
  (:import goog.History))

(def initial-state {:view nil})

(defonce !app (reagent/atom initial-state))

;; TODO remove all these page functions
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

;; figwheel reload-hook
(defn reload-hook
  []
  (swap! !app identity))

(defn render-fn
  [ui-channel app]
  (reagent/render [view/root ui-channel app]
                  js/document.body))

(defn init!
  []
  (petrol/start-message-loop! !app
                              render-fn
                              #{(petrol-routing/init routes/app-routes)}))
