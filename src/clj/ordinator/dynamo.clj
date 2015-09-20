(ns ordinator.dynamo
  (:require [taoensso.faraday :as far]))

(def client-opts
  {;;; For DDB Local just use some random strings here, otherwise include your
   ;;; production IAM keys:
   :access-key "dummy-ordinator-aws-key"
   :secret-key "dummy-ordinator-aws-secret"

   :endpoint "http://localhost:8000"
   ;:endpoint "http://dynamodb.eu-west-1.amazonaws.com"
  })

(defn ensure-tables
  []
  (far/ensure-table client-opts :orders
                    [:user :s]
                    {:range-keydef [:orderdate :s]
                     :throughput {:read 1 :write 1}
                     :block? true}))

(defn get-user-order
  [user orderdate]
  (far/get-item client-opts
                :orders
                {:user user :orderdate orderdate}))

(defn save-user-order
  [{:keys [user orderdate items]}]
  (prn "user: " user " orderdate: " orderdate " items" items)
  (far/put-item client-opts
                :orders
                {:user user
                 :orderdate orderdate
                 :items items}))
