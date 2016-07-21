(ns cljs.ordinator.order.processing
  (:require [petrol.core :refer [Message EventSource]]
            [cljs.ordinator.order.messages :as m]
            [cljs.ordinator.order.rest :as rest]
            [ordinator.utils :as u]))

(extend-protocol Message

  m/GetOrder
  (process-message [_ app]
    (prn "GetOrder [Message] app" app) app)

  m/OrderResult
  (process-message [{:keys [status body] :as response} app]
    (when (= status 200)
      (let [{:keys [orderdate user items]} body]
        (assoc app :orderdate orderdate :user user :items items))))

  m/DeleteOrderLine
  (process-message [{code :code} app]
    (update-in app [:items] dissoc code))

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
          itemdata (assoc (code (:catalogue app)) :estcost nil :quantity nil :codestr codestr)]
      (assoc app :order-item itemdata)))

  m/ChangeQuantity
  (process-message [{:keys [quantity]} app]
    (let [{:keys [unitsperpack price]} (:order-item app)
          estcost (* (/ quantity unitsperpack) price)]
      (prn "ChangeQuantity upp price quantity estcost" unitsperpack price quantity estcost)
      (update-in app [:order-item] assoc :estcost estcost :quantity quantity)))

  m/AddItem
  (process-message [_ app]
    (let [{:keys [order-item items]} app
          code (u/code->key (:code order-item))
          items (assoc items code order-item)]
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
    (prn "GetOrder [EventSource] username" username)
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
