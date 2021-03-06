(ns ordinator.auth
  (:require [ordinator
             [role :as role]
             [dynamo :as db]]
            [cemerick.friend :as friend]
            [cemerick.friend
             [workflows :as workflows]
             [credentials :as creds]]
            [marianoguerra.friend-json-workflow :as json-auth]
            [radix.error :refer [error-response]]))

; a dummy in-memory user "database"
(def users {"root" {:userid "0-0"
                    :username "root"
                    :password (creds/hash-bcrypt "admin")
                    :roles #{::role/admin}}
            "jerzy" {:userid "1a"
                     :username "jerzy"
                     :password (creds/hash-bcrypt "albany")
                     :roles #{::role/user ::role/admin}}
            "sally" {:userid "2b"
                     :username "sally"
                     :password (creds/hash-bcrypt "albany")
                     :roles #{::role/user ::role/coordinator}}
            "matthew" {:userid "3c"
                       :username "matthew"
                       :password (creds/hash-bcrypt "albany")
                       :roles #{::role/user}}})

(defn user-list
  []
  (let [users (db/get-active-users)
        ids (map :userid users)
        names (map :username users)]
    (zipmap ids names)))

(defn unauthenticated-handler [_]
  "handler when authentication fails"
  (error-response "You need to be logged in to use this resource" 401))

(defn unauthorized-handler [_]
  "handler when authorization fails"
  (error-response "Access to this resource isn't allowed" 403))

(defn wrap-json-authenticate
  [handler]
  (friend/authenticate handler
                       {:login-uri "/v1/login"
                        :default-landing-uri "/v1/login"
                        :unauthorized-handler unauthorized-handler
                        :unauthenticated-handler unauthenticated-handler
                        :redirect-on-auth? false
                        :workflows [(json-auth/json-login
                                     :login-uri "/v1/login"
                                     :login-failure-handler json-auth/login-failed
                                     :credential-fn (fn [{:keys [username] :as login-data}]
                                                      (let [user-rec (db/get-user-by-username username)
                                                            user-creds {username user-rec}]
                                                        (creds/bcrypt-credential-fn user-creds login-data)
                                                        ;(creds/bcrypt-credential-fn users login-data)
                                                        )))]}))

(defn wrap-same-user
  [handler userid]
  (fn [req]
    (let [auth-userid (:userid (friend/current-authentication req))]
      (if (= userid auth-userid)
        (handler req)
        (error-response "You may not access a different user's resources" 400)))))
