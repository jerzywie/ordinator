(ns ordinator.order
  (:require [ordinator.dynamo :as db]
            [clojure.string :refer [upper-case]]))

(defn ensure-map
  [a-map]
  (if (seq a-map)
    a-map
    {:items nil}))

(defn clean-order-item
  [[code {:keys [itemdata order]}]]
  {code {:itemdata (select-keys itemdata [:origin :description :packsize :price
                                          :unit :unitsperpack :vat :splits?])
         :order order}})

(defn augment-order-item
  [[code {:keys [itemdata order]}]]
  {code {:itemdata (assoc itemdata :codestr (-> code name upper-case))
         :order order}})

(defn add-other-fields
  [{:keys [items] :as order}]
  (into {} (map augment-order-item items)))

(defn get-user-order
  [userid orderdate]
  (->> (db/get-user-order userid orderdate)
       (ensure-map)
       (add-other-fields)
       (assoc {} :userid userid :orderdate orderdate :items)))

(defn save-user-order
  [{:keys [user orderdate items]}]
  (->> (map clean-order-item items)
       (into {})
       (db/save-user-order user orderdate)))
