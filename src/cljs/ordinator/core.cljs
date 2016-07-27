(ns cljs.ordinator.core
  (:require [petrol.core :as petrol]
            [petrol.routing :as petrol-routing]
            [cljs.ordinator.routes :as routes]
            [cljs.ordinator.processing]
            [cljs.ordinator.view :as view]
            [cljs.ordinator.login.core :as login]
            [cljs.ordinator.order.core :as order]
            [cljs.ordinator.allorders.core :as allorders]
            [ordinator.utils :as utils]
            [reagent.core :as reagent]
            [goog.events :as events]
            [goog.history.EventType :as EventType])
  (:import goog.History))

(def initial-state {:view {:handler :login-page}
                    :login login/initial-state
                    :order order/initial-state
                    :allorders allorders/initial-state})

(defonce !app (reagent/atom initial-state))

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
