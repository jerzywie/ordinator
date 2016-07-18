(ns cljs.ordinator.order.processing
  (:require [petrol.core :refer [Message EventSource]]
            [cljs.ordinator.order.messages :as m]
            [cljs.ordinator.order.rest :as rest]))

(extend-protocol Message
  m/GetOrder
  (process-message [_ app]
    (prn "GetOrder [Message] app" app) app))

(extend-protocol EventSource
  m/GetOrder
  (watch-channels [{:keys [username]} app]
    (prn "GetOrder [EventSource] username" username)
    #{(rest/retrieve-order username "current")}))

(extend-protocol Message
  m/OrderResult
  (process-message [{:keys [status body] :as response} app]
    (when (= status 200)
      (let [{:keys [orderdate user items]} body]
        (assoc app :orderdate orderdate :user user :items items)))))
