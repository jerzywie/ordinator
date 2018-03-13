(ns ordinator.user
  (:require [ordinator.dynamo :as db]))

(defn create-user
  [{:keys [name username email]}]
  (let [userid (str (java.util.UUID/randomUUID))]
    (prn "Creating user. name" name "username" username "email" email "userid" userid)
    {:userid userid :name name :username username :email email}))

(defn find-user-by-username
  [username])
