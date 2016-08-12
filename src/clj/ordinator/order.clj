(ns ordinator.order
  (:require [ordinator.dynamo :as db]
            [clojure.string :refer [upper-case]]))

(defn ensure-map
  [a-map]
  (if (seq a-map)
    a-map
    {:items nil}))

(defn clean-order-item
  [[code item]]
  {code (select-keys item [:quantity :estcost :origin :description :packsize :price
                           :unit :unitsperpack :vat :splits?])})

(defn augment-order-item
  [[code item]]
  {code (assoc item :codestr (-> code name upper-case))})

(defn add-other-fields
  [{:keys [items] :as order}]
  (into {} (map augment-order-item items)))

(defn get-user-order
  [user orderdate]
  (->> (db/get-user-order user orderdate)
       (ensure-map)
       (add-other-fields)
       (assoc {} :user user :orderdate orderdate :items)))

(defn save-user-order
  [{:keys [user orderdate items]}]
  (->> (map clean-order-item items)
       (into {})
       (db/save-user-order user orderdate)))
