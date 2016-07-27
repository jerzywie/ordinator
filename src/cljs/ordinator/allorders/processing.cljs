(ns cljs.ordinator.allorders.processing
  (:require [petrol.core :refer [Message EventSource]]
            [cljs.ordinator.allorders.messages :as m]
            [cljs.ordinator.allorders.rest :as rest]
            [ordinator.utils :as u]))

(extend-protocol Message

  m/GetAllOrders
  (process-message [_ app]
    app)

  m/GetAllOrdersResult
  (process-message [{:keys [status body] :as response} app]
    (when (= status 200)
      (let [{:keys [orderdate items]} body]
        (assoc app :orderdate orderdate :items items))))

  m/EditItem
  (process-message [{:keys [code]} app]
    (prn "EditItem code" code)
    (assoc app :editing code))

  m/StopEditing
  (process-message [_ app]
    (prn "StopEditing")
    ;(assoc app :editing nil)
    app)

  m/ChangeQuantity
  (process-message [{:keys [member quantity]} app]
    (prn "ChangeQantity member" member "quantity" quantity)
    (let [code (:editing app)
          qty-cost {:quantity quantity :estcost 123.45}]
      (assoc-in app [:items code :orders member] qty-cost)))

  m/KeyEvent
  (process-message [{:keys [c]} app]
    (case c
      27 (assoc app :editing nil)
      app)))


(extend-protocol EventSource

  m/GetAllOrders
  (watch-channels [{:keys [orderdate]} app]
    #{(rest/get-allorders orderdate)}))
