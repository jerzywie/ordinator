(ns cljs.ordinator.order.processing
  (:require [petrol.core :refer [Message EventSource]]
            [cljs.ordinator.order.messages :as m]
            [cljs.ordinator.order.rest :as rest]
            [clojure.string :refer [upper-case]]
            [ordinator.utils :as u]))

(extend-protocol Message

  m/GetOrder
  (process-message [_ app]
    app)

  m/OrderResult
  (process-message [{:keys [status body] :as response} app]
    (when (= status 200)
      (let [{:keys [orderdate user items]} body]
        (assoc app :orderdate orderdate :user user :items items))))

  m/DeleteOrderLine
  (process-message [{code :code} app]
    (update-in app [:items] dissoc code))

  m/EditOrderLine
  (process-message [{code :code} app]
    (assoc app :order-item (get-in app [:items code])))

  m/GetCatalogue
  (process-message [_ app]
    app)

  m/GetCatalogueResult
  (process-message [{:keys [status body]} app]
    (when (= status 200)
      (assoc app :catalogue body)))

  m/ChangeCode
  (process-message [{:keys [codestr]} app]
    (let [code (u/code->key codestr)
          itemdata (assoc (code (:catalogue app)) :codestr codestr)]
      (assoc app :order-item {:itemdata itemdata :order {:estcost nil :quantity nil}})))

  m/ChangeQuantity
  (process-message [{:keys [quantity]} app]
    (let [quantity (u/tofloat quantity)
          {{:keys [unitsperpack price vat]} :itemdata} (:order-item app)
          estcost (u/cost-to-user quantity unitsperpack price vat)]
      (update-in app [:order-item :order] assoc :estcost estcost :quantity quantity)))

  m/AddItem
  (process-message [_ app]
    (let [{{:keys [itemdata order]} :order-item items :items} app
          codestr (upper-case (:codestr itemdata))
          code (u/code->key codestr)
          itemdata (assoc itemdata :codestr codestr)
          items (assoc items code {:itemdata itemdata :order order})]
      (assoc app :items items :order-item nil :isdirty true)))

  m/SaveOrder
  (process-message [_ app]
    app)

  m/SaveOrderResult
  (process-message [{:keys [status]} app]
    (when (= status 200)
      (assoc app :isdirty false))))


(extend-protocol EventSource

  m/GetOrder
  (watch-channels [{:keys [username]} app]
    #{(rest/retrieve-order username "current")})

  m/GetCatalogue
  (watch-channels [_ app]
    (when (nil? (:catalogue app))
      #{(rest/read-catalogue)}))

  m/SaveOrder
  (watch-channels [_ app]
    (let [username (:user app)
          orderdate "current"
          order-items (:items app)]
      #{(rest/save-order username orderdate order-items)})))
