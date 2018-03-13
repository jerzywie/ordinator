(ns cljs.ordinator.processing
  (:require [cljs.core.async :refer [to-chan chan]]
            [petrol.core :refer [Message
                                 process-message
                                 process-submessage
                                 EventSource
                                 watch-channels
                                 watch-subchannels]]
            [petrol.routing :refer [UrlHistoryEvent]]
            [cljs.ordinator.messages :as m]
            [cljs.ordinator.order.messages :as order-m]
            [cljs.ordinator.allorders.messages :as allorders-m]
            [cljs.ordinator.login.processing]
            [cljs.ordinator.order.processing]
            [cljs.ordinator.allorders.processing]))

(extend-protocol Message

  m/Login
  (process-message [{:keys [submessage]} app]
    (process-submessage submessage app [:login]))

  m/Order
  (process-message [{:keys [submessage]} app]
    (process-submessage submessage app [:order]))

  m/AllOrders
  (process-message [{:keys [submessage]} app]
    (process-submessage submessage app [:allorders])))

(extend-protocol EventSource

  UrlHistoryEvent
  (watch-channels [{current-view :view} {:keys [view] :as app}]
    (case (:handler current-view)
      :order-page #{(to-chan [(m/->NavigateToOrder (:order app))])}
      :allorders-page #{(to-chan [(m/->NavigateToAllOrders (:allorders app))])}
      nil))

  m/Login
  (watch-channels [{:keys [submessage]} app]
    (watch-subchannels submessage app [:login] m/->Login))

  m/Order
  (watch-channels [{:keys [submessage]} app]
    (watch-subchannels submessage app [:order] m/->Order))

  m/AllOrders
  (watch-channels [{:keys [submessage]} app]
    (watch-subchannels submessage app [:allorders] m/->AllOrders))

  m/NavigateToOrder
  (watch-channels [_ app]
    (let [logged-in? (get-in app [:login :loggedin])]
      (when logged-in?
        (prn "NavigateToOrder app" (update-in app [:order] dissoc :catalogue))
        (prn "NTO userid " (get-in app [:login :user :userid]))
        #{(to-chan [(m/->Order (order-m/->GetCatalogue))
                    (m/->Order (order-m/->GetOrder (get-in app [:login :user :userid])))])})))

  m/NavigateToAllOrders
  (watch-channels [_ app]
    (let [logged-in? (get-in app [:login :loggedin])]
      (when logged-in?
        #{(to-chan [(m/->AllOrders (allorders-m/->GetAllOrders "current"))])}))))
