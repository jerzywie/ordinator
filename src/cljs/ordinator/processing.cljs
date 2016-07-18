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
            [cljs.ordinator.login.processing]
            [cljs.ordinator.order.processing]))

(extend-protocol Message

  m/Login
  (process-message [{:keys [submessage]} app]
    (process-submessage submessage app [:login]))

  m/Order
  (process-message [{:keys [submessage]} app]
    (process-submessage submessage app [:order])))

(extend-protocol EventSource

  UrlHistoryEvent
  (watch-channels [{current-view :view} {:keys [view] :as app}]
    (prn "routes UHE current-view" current-view "view" view "app" app)
    (case (:handler current-view)
      :order-page #{(to-chan [(m/->NavigateToOrder (:order app))])}
      nil))

  m/Login
  (watch-channels [{:keys [submessage]} app]
    (watch-subchannels submessage app [:login] m/->Login))

  m/Order
  (watch-channels [{:keys [submessage]} app]
    (watch-subchannels submessage app [:order] m/->Order))

  m/NavigateToOrder
  (watch-channels [order-app app]
    (let [logged-in? (get-in app [:login :loggedin])]
      (when logged-in?
        #{(to-chan [(m/->Order (order-m/->GetOrder (get-in app [:login :user :username])))])}))))
