(ns ordinator.collation
  (:require [ordinator
             [dynamo :as db]
             [auth :refer [user-list]]]))

(defn collate
  "Helper for merge-with. Assumes that the
   itemdata in result and latter are the same."
  [val-in-result val-in-latter]
  (let [{:keys [itemdata orders]} val-in-result
        new-orders (:orders val-in-latter)]
    {:itemdata itemdata
     :orders (merge orders new-orders)}))

(defn itemdata-and-orders
  [user [code {:keys [quantity estcost] :as itemdata}]]
  {code {:itemdata (dissoc itemdata :quantity :estcost)
         :orders {(keyword user) {:quantity quantity :estcost estcost}}}})

(defn get-orders
  [{:keys [user items]}]
  (map #(itemdata-and-orders user %) items))

(defn collate-orders
  [orderdate]
  (->> (user-list)
      (map #(db/get-user-order % orderdate))
      (map get-orders)
      flatten
      (apply merge-with (partial collate))
      (assoc {:orderdate orderdate} :items)))
