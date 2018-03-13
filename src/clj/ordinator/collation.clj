(ns ordinator.collation
  (:require [ordinator
             [dynamo :as db]
             [auth :refer [user-list]]]
            [clojure.string :refer [lower-case]]))

(defn collate
  "Helper for merge-with. Assumes that the
   itemdata in result and latter are the same."
  [val-in-result val-in-latter]
  (let [{:keys [itemdata orders]} val-in-result
        new-orders (:orders val-in-latter)]
    {:itemdata itemdata
     :orders (merge orders new-orders)}))

(defn itemdata-and-orders
  [user [code {:keys [itemdata order]}]]
  {code {:itemdata itemdata
         :orders {(keyword user) order}}})

(defn get-orders
  [{:keys [user items]}]
  (map #(itemdata-and-orders user %) items))

(defn collate-orders
  [orderdate]
  (let [users (user-list)
        id-list (keys users)]
    (prn "users" (user-list) "id-list" id-list)
    (->> id-list
         (map #(db/get-user-order % orderdate))
         (map get-orders)
         flatten
         (apply merge-with (partial collate))
         (assoc {:orderdate orderdate :userlist users} :items))))

(defn merge-new-order
  [code itemdata new-order old-order]
  (assoc old-order code {:itemdata itemdata :order new-order}))

(defn update-user-order-line
  [user code itemdata new-order]
  (let [orderdate "current"]
    (->> (:items (db/get-user-order user orderdate))
         (merge-new-order code itemdata new-order)
         (db/save-user-order user orderdate))))

(defn update-order-line
  [{{:keys [itemdata orders]} :orders code :code orderdate :orderdate}]
  (let [code (-> code lower-case keyword)]
    (prn "update-order-line orderdate" orderdate "code" code "itemdata" itemdata "orders" orders)
    (dorun (map #(update-user-order-line % code itemdata (% orders)) (keys orders)))))

(defn delete-user-order-line
  [user orderdate code]
  (let [remove-code (fn [code map] (dissoc map code))]
    (->> (:items (db/get-user-order user orderdate))
         (remove-code code)
         (db/save-user-order user orderdate))))

(defn delete-order-line
  [orderdate code]
  (let [code (-> code lower-case keyword)]
    (prn "delete-order-line orderdate" orderdate "code" code)
    (dorun (map #(delete-user-order-line % orderdate code) (user-list)))))
