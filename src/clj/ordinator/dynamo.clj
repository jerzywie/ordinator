(ns ordinator.dynamo
  (:require [taoensso.faraday :as far]
            [environ.core :refer [env]]))

(def client-opts (let [dev-access-key (env :dev-access-key)
                       dev-secret-key (env :dev-secret-key)]
                   (merge {:endpoint (env :dynamodb-endpoint "http://localhost:8000")}
                          (when (and dev-access-key dev-secret-key)
                            {:access-key dev-access-key :secret-key dev-secret-key}))))

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

(defn- keywordize-roles
  [roles]
  (->> roles (map keyword) set))

(defn get-user-by-userid
  "Retrieves a user record by userid."
  [userid]
  (let [result (far/get-item client-opts
                             :users
                             {:userid userid})]
    (when result
      (let [kr (keywordize-roles (:roles result))]
        (assoc result :roles kr)))))

(defn get-user-by-username
  "Retrieves a user record by username."
  [username]
  (first (far/query client-opts
                    :users
                    {:username [:eq  (keyword username)]}
                    {:index "username-index"})))

(defn create-user
  "Create a new user.

   Constraints:
   userid and username must be unique."
  [{:keys [userid username] :as user-record}]
  (cond
    (get-user-by-userid userid) (throw (Exception. (str "Userid " userid " already exists.")))
    (get-user-by-username username) (throw (Exception. (str "Username " username " already exists.")))
    :else (far/put-item client-opts
                        :users
                        user-record))
  user-record)

(defn update-user
  "Update user details.

  Constraints:
  If username is supplied it must be unique.
  If password is supplied it is encrypted."
  [{:keys [userid username password] :as update-details}]
  (let [current-details (get-user-by-userid userid)
        current-username (:username current-details)]
    (when-not current-details (throw (Exception. (str "User " userid " does not exist."))))
    (when username
      (when-let [username-details (get-user-by-username username)]
        (if (not= userid (:userid username-details))
          (throw (Exception. (str "Username " userid " is already in use."))))))
    (let [updated-record (merge current-details update-details)]
      (far/put-item client-opts
                    :users
                    updated-record)
      updated-record)))

(defn get-active-users
  "Return all users marked as active."
  []
  (far/scan client-opts
            :users
            {:attr-conds {:active? [:eq true]}
                                   :return [:userid :username]}))
