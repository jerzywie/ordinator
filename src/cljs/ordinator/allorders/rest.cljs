(ns cljs.ordinator.allorders.rest
  (:require [cljs-http.client :as http]
            [petrol.core :as petrol]
            [cljs.ordinator.allorders.messages :as m]))

(defn get-allorders
  "Get collation of all orders for orderdate"
  [orderdate]
  (let [resource (str  "/orders/" orderdate)
        response (http/get resource)]
    (petrol/wrap m/map->GetAllOrdersResult response)))

(defn save-order-line
  "Save a single order-line"
  [code orders]
  (prn "save-order-line")
  (let [resource (str "/orders/current/" code)
        response (http/put resource
                           {:json-params {:orders orders}})]
    (petrol/wrap m/map->SaveItemResult response)))

(defn delete-order-line
  "Delete a single order-line"
  [code]
  (prn "delete-order-line")
  (let [resource (str "/orders/current/" code)
        response (http/delete resource)]
    (petrol/wrap m/map->ReallyDeleteItemResult response)))
