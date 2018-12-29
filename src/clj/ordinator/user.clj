(ns ordinator.user
  (:require [ordinator.dynamo :as db]))

(defn create-user
  [{:keys [username name email]}]
  (let [userid "userid-xyz";(str (java.util.UUID/randomUUID))
        ]
    (prn "Creating user. name" name "username" username "email" email "userid" userid)
    (db/save-user userid username name email)))

(defn find-user-by-username
  [username])
