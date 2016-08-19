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
        (assoc :editing nil :preeditorders nil :isdirty nil)
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
    (let [{:keys [deleteconfirm editing]} app]
      (if-not (u/disable-it deleteconfirm editing code)
        (assoc app :editing code :preeditorders (get-in app [:items code :orders]))
        app)))

  m/RevertEditing
  (process-message [_ app]
    (prn "RevertEditing")
    (revert-editing app))

  m/ChangeQuantity
  (process-message [{:keys [member quantity]} app]
    (prn "ChangeQantity member" member "quantity" quantity)
    (let [quantity (u/tofloat quantity)
          code (:editing app)
          {:keys [unitsperpack price vat]} (get-in app [:items code :itemdata])
          estcost (u/cost-to-user quantity unitsperpack price vat)
          qty-cost {:quantity quantity :estcost estcost}]
      (-> app
          (assoc-in [:items code :orders member] qty-cost)
          (assoc :isdirty true))))

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
    (assoc app :editing nil :preeditorders nil :isdirty nil))

  m/ConfirmDeleteItem
  (process-message [{:keys [code]} app]
    (assoc app :deleteconfirm code))

  m/CancelDeleteItem
  (process-message [_ app]
    (assoc app :deleteconfirm nil))

  m/ReallyDeleteItem
  (process-message [_ app]
    app)

  m/ReallyDeleteItemResult
  (process-message [{:keys [status body] :as response} app]
    (assoc app :deleteconfirm nil)))

(extend-protocol EventSource

  m/GetAllOrders
  (watch-channels [{:keys [orderdate]} app]
    #{(rest/get-allorders orderdate)})

  m/SaveItem
  (watch-channels [{:keys [code]} app]
    #{(rest/save-order-line (u/key->code code) (get-in app [:items code]))})

  m/ReallyDeleteItem
  (watch-channels [{:keys [code]} app]
    #{(rest/delete-order-line (u/key->code code))
      (to-chan [(m/->GetAllOrders "current")])}))
