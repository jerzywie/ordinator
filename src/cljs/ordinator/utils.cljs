(ns ordinator.utils
  (:require [reagent.core :as reagent]
            [cljs.core.async :refer [chan <! close!]]
            [cljs-http.client :as http]
            [clojure.string :as s]
            [goog.string :as gstring]
            [goog.string.format :as gformat])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; New utils go here

(defn navigate! [hash]
  (set! (.-hash js/location) hash))

(defn code->key
  [code]
  (-> code s/trim s/lower-case keyword))

;;==============================================================================
;; TODO remove everything below here, eventually
;;==============================================================================
(def appstate
  "appstate has the following structure:
   {:user {:username ...
           :roles ... }
    :message \"any auth failure message\"
    :order {:item1code {item1data}
            :item2code {item2data}
            ...}
    :allorders {
      :orderdate date
      :items {
        :item1code {
          :itemdata {item1-general-data}
          :orders {
             :member1code {member1-order}
             :member2code {member2-order}
          }}}}}"

(reagent/atom nil))

(defn reset-appstate!
 []
 (reset! appstate nil))

(defn logged-in?
  []
  (not (nil? (:user @appstate))))

(defn get-username
  []
  (when (logged-in?)
    (get-in @appstate [:user :username])))

(defn has-role?
  [role]
  (when (logged-in?)
    (some #{role} (get-in appstate [:user :roles]))))

(defn get-message
  []
  (:message @appstate))

(defn get-order-items
  []
  (:order @appstate))

(defn get-order! [user order-date]
  "Get order data.
   result is the ratom to hold the data"
  (go
    (when (nil? (get-order-items))
      (prn "getting order")
      (let [user (get-username)
            orderdate "current"
            path "/users/%s/orders/%s"
            resource (gstring/format path user orderdate)
            {:keys [status body] :as response} (<! (http/get resource))]
        (swap! appstate assoc  :order (:items body))))))

(defn delete-order-item
  [code]
  (swap! appstate update-in [:order] dissoc code))

(defn add-order-item
  [code value]
  (swap! appstate assoc-in [:order code] value))

(defn save-order []
  "Save order data."
  (prn "saving order.1")
  (when-let [order-items (get-order-items)]
    (let [user (get-username)
          orderdate "current"
          path "/users/%s/orders/%s"
          resource (gstring/format path user orderdate)]
      (prn "saving order")
      (http/put resource
                {:json-params {:user (keyword user) :orderdate (keyword orderdate)
                               :items order-items}}))))

(defn all-orders
  []
  (:allorders @appstate))

(defn get-allorders!
  [orderdate]
  (go
    (let [path "/orders/%s"
          resource (gstring/format path orderdate)
          {:keys [status body] :as response} (<! (http/get resource))]
      (prn "get-allorders! body" body)
      (swap! appstate assoc  :allorders body))))
