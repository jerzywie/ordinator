(ns ordinator.utils
  (:require [reagent.core :as r]
            [cljs.core.async :refer [chan <! close!]]
            [cljs-http.client :as http]
            [goog.string :as gstring]
            [goog.string.format :as gformat])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def appstate
  "appstate has the following structure:
   {:user {:username ...
           :roles ... }
    :message \"any auth failure message\"
    :order {:item1code {item1data}
            :item2code {item2data}
            ...}"
  (r/atom nil))

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

(defn do-login!
  [username password]
  (go
    (let [{:keys [status body] :as response} (<! (http/post
                                                  "/login"
                                                  {:json-params
                                                   {:username username :password password}}))]
      (case status
        201 (reset! appstate {:user body})
        (reset! appstate {:message (:reason body)})))))

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
