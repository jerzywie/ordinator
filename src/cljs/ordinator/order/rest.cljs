(ns cljs.ordinator.order.rest
  (:require [cljs-http.client :as http]
            [petrol.core :as petrol]
            [cljs.ordinator.order.messages :as m]))

(defn retrieve-order
  "Get order and save in local cache"
  [username orderdate]
  (let [orderdate "current"
        resource (str "/users/" username "/orders/" orderdate)
        response (http/get resource)]
    (petrol/wrap m/map->OrderResult response)))
