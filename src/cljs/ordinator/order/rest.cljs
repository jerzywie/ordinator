(ns cljs.ordinator.order.rest
  (:require [cljs-http.client :as http]
            [petrol.core :as petrol]
            [cljs.ordinator.order.messages :as m]))

(defn retrieve-order
  "Get order and save in local cache"
  [userid orderdate]
  (prn "rest/retrieve-order userid" userid)
  (let [orderdate "current"
        resource (str "/v1/users/" userid "/orders/" orderdate)
        response (http/get resource)]
    (prn "rest/retrieve-order response" response)
    (petrol/wrap m/map->OrderResult response)))

(defn read-catalogue
  []
   "Get catalogue data."
   (prn "getting catalogue!!")
   (let [response (http/get "/v1/catalogue")]
     (petrol/wrap m/map->GetCatalogueResult response)))

(defn save-order
  [userid orderdate order-items]
  "Save order data."
  (prn "rest/save order")
  (let [resource (str "/v1/users/" userid "/orders/" orderdate)
        response (http/put resource
                           {:json-params {:user (keyword userid) :orderdate (keyword orderdate)
                                          :items order-items}})]
    (petrol/wrap m/map->SaveOrderResult response)))
