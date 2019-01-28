(ns ordinator.user
  (:require [ordinator.dynamo :as db]
            [cemerick.friend.credentials :as creds]
            [re-rand :refer [re-rand]]))

(defn user-id
  []
  (re-rand #"[a-z0-9]{8}"))

(defn create-user
  [{:keys [username name password email roles] :as user-record}]
  (cond (nil? username) (throw (Exception. "Username must be supplied."))
        (nil? name) (throw (Exception. "Name must be supplied."))
        (nil? password) (throw (Exception. "Password must be supplied."))
        (nil? email) (throw (Exception. "Email must be supplied."))
        (nil? roles) (throw (Exception. "Roles must be supplied"))
        :else (let [userid (user-id)
                    hash-password (creds/hash-bcrypt password)
                    complete-record (assoc user-record :userid userid
                                           :password hash-password
                                           :active? true)]
                (db/create-user complete-record))))

(defn find-user-by-username
  [username]
  (db/get-user-by-username username))

(defn update-user
  "Update user details.

  Constraints:
  If username is supplied it must be unique.
  If password is supplied it is encrypted."
  [userid {:keys [password] :as update-details}]
  (let [update-details (if password (assoc update-details :password (creds/hash-bcrypt password)) update-details)]
   (db/update-user (assoc update-details :userid userid))))

(defn disable-user
  "Set active? flag to false"
  [userid]
  (db/update-user {:userid userid :active? false}))

(defn get-active-users
  "Get all active users"
  []
  (db/get-active-users))
