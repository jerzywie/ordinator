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
