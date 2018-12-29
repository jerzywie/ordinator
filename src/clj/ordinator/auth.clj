(ns ordinator.auth
  (:require [cemerick.friend :as friend]
            [cemerick.friend
             [workflows :as workflows]
             [credentials :as creds]]
            [marianoguerra.friend-json-workflow :as json-auth]
            [radix.error :refer [error-response]]))

; a dummy in-memory user "database"
(def users {"root" {:userid "0-0"
                    :username "root"
                    :password (creds/hash-bcrypt "admin")
                    :roles #{::admin}}
            "jerzy" {:userid "1a"
                     :username "jerzy"
                     :password (creds/hash-bcrypt "albany")
                     :roles #{::user ::admin}}
            "sally" {:userid "2b"
                     :username "sally"
                     :password (creds/hash-bcrypt "albany")
                     :roles #{::user ::coordinator}}
            "matthew" {:userid "3c"
                       :username "matthew"
                       :password (creds/hash-bcrypt "albany")
                       :roles #{::user}}})

(derive ::admin ::user)

(derive ::admin ::coordinator)

(derive ::coordinator ::user)

(defn user-list
  []
  (let [ord-users (rest users)
        names (keys ord-users)
        ids (map :userid (vals ord-users))]
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
                                     :credential-fn (partial creds/bcrypt-credential-fn users))]}))

(defn wrap-same-user
  [handler userid]
  (fn [req]
    (let [auth-userid (:userid (friend/current-authentication req))]
      (if (= userid auth-userid)
        (handler req)
        (error-response "You may not access a different user's resources" 400)))))
