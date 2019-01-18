(ns ordinator.user
  (:require [ordinator.dynamo :as db]
            [cemerick.friend.credentials :as creds]))

(defn create-user
  [{:keys [username password] :as user-record}]
  (let [userid (str (java.util.UUID/randomUUID))
        hash-password (creds/hash-bcrypt password)
        complete-record (assoc user-record :userid userid :password hash-password :active? true)]
    (prn "Creating user. user-record: " complete-record)
    (db/create-user complete-record)))

(defn find-user-by-username
  [username]
  (db/get-user-by-username username))

(defn update-user
  "Update user details.

  Constraints:
  If username is supplied it must be unique.
  If password is supplied it is encrypted."
  [{:keys [userid password] :as update-details}]
  (cond
    (nil? userid) (throw (Exception. "Userid must be supplied."))
    :else ((when password (assoc update-details :password (creds/hash-bcrypt password)))
           (db/update-user update-details))))
