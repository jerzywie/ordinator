(ns ordinator.dynamo
  (:require [taoensso.faraday :as far]))

(def client-opts
  {;;; For DDB Local just use some random strings here, otherwise include your
   ;;; production IAM keys:
   :access-key "dummy-ordinator-aws-key"
   :secret-key "dummy-ordinator-aws-secret"

   :endpoint "http://localhost:8000"
   :region "a-region"
   ;:endpoint "http://dynamodb.eu-west-1.amazonaws.com"
  })

(defn ensure-tables
  []
  (far/ensure-table client-opts :orders
                    [:userid :s]
                    {:range-keydef [:orderdate :s]
                     :throughput {:read 1 :write 1}
                     :block? true})
  (far/ensure-table client-opts :users
                    [:userid :s]
                    {:gsindexes [{:name "username-index"
                                  :hash-keydef [:username :s]
                                  :projection :keys-only
                                  :throughput {:read 1 :write 1}}]
                     :throughput {:read 1 :write 1}
                     :block? true}))

(defn get-user-order
  [userid orderdate]
  (far/get-item client-opts
                :orders
                {:userid userid :orderdate orderdate}))

(defn save-user-order
  [userid orderdate items]
  (prn "save-user-order userid" userid "orderdate" orderdate "items" items )
  (far/put-item client-opts
                :orders
                {:userid userid
                 :orderdate orderdate
                 :items items}))

(defn keywordize-roles
  [roles]
  (->> roles (map keyword) set))

(defn get-user-by-userid
  "Retrieves a user record by userid."
  [userid]
  (let [result (far/get-item client-opts
                             :users
                             {:userid userid})]
    (when result
      (prn "get-user-by-userid result: " result)
      (let [kr (keywordize-roles (:roles result))]
        (assoc result :roles kr)))))

(defn save-user
  [userid username name email roles]
  (if-not (get-user-by-userid userid)
    (let [user-record {:userid userid
                       :username username
                       :name name
                       :email email
                       :roles roles}]
      (prn "save-user received " user-record)
      (far/put-item client-opts
                    :users
                    user-record)
      (prn "save-user returning " user-record)
      user-record)
    (throw (Exception. (str "Userid " userid " already exists.")))))
