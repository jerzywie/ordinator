(ns cljs.ordinator.allorders.processing
  (:require [cljs.core.async :refer [to-chan]]
            [petrol.core :refer [Message EventSource]]
            [cljs.ordinator.allorders.messages :as m]
            [cljs.ordinator.allorders.rest :as rest]
            [ordinator.utils :as u]))


(defn revert-editing
  [app]
  (let [pre-edit-orders (:preeditorders app)
        code (:editing app)]
    (-> app
        (assoc :editing nil :preeditorders nil)
        (assoc-in [:items code :orders] pre-edit-orders))))

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
    (assoc app :editing code :preeditorders (get-in app [:items code :orders])))

  m/RevertEditing
  (process-message [_ app]
    (prn "RevertEditing")
    (revert-editing app))

  m/ChangeQuantity
  (process-message [{:keys [member quantity]} app]
    (prn "ChangeQantity member" member "quantity" quantity)
    (let [quantity (u/tofloat quantity)
          code (:editing app)
          {:keys [unitsperpack price]} (get-in app [:items code :itemdata])
          estcost (u/cost-to-user quantity unitsperpack price)
          qty-cost {:quantity quantity :estcost estcost}]
      (assoc-in app [:items code :orders member] qty-cost)))

  m/KeyEvent
  (process-message [{:keys [c]} app]
    (case c
      27 (revert-editing app)
      app))

  m/DoNothing
  (process-message [_ app]
    app)

  m/SaveItem
  (process-message [_ app]
    app)

  m/SaveItemResult
  (process-message [{:keys [status body] :as response} app]
    (assoc app :editing nil :preeditorders nil)))


(extend-protocol EventSource

  m/GetAllOrders
  (watch-channels [{:keys [orderdate]} app]
    #{(rest/get-allorders orderdate)})

  m/SaveItem
  (watch-channels [{:keys [code]} app]
    #{(rest/save-order-line (u/key->code code) (get-in app [:items code]))}))
