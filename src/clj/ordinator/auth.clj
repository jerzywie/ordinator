(ns ordinator.auth
  (:require [cemerick.friend :as friend]
            [cemerick.friend
             [workflows :as workflows]
             [credentials :as creds]]
            [marianoguerra.friend-json-workflow :as json-auth]
            [radix.error :refer [error-response]]))

; a dummy in-memory user "database"
(def users {"root" {:id 0
                    :username "root"
                    :password (creds/hash-bcrypt "admin")
                    :roles #{::admin}}
            "jerzy" {:id 1
                     :username "jerzy"
                     :password (creds/hash-bcrypt "albany")
                     :roles #{::user ::admin}}
            "sally" {:id 2
                     :username "sally"
                     :password (creds/hash-bcrypt "albany")
                     :roles #{::user ::coordinator}}
            "matthew" {:id 3
                     :username "matthew"
                     :password (creds/hash-bcrypt "albany")
                     :roles #{::user}}})

(derive ::admin ::user)

(derive ::admin ::coordinator)

(derive ::coordinator ::user)

(defn user-list
  []
  (into [] (rest (keys users))))

(defn unauthenticated-handler [_]
  "handler when authentication fails"
  (error-response "You need to be logged in to use this resource" 401))

(defn unauthorized-handler [_]
  "handler when authorization fails"
  (error-response "Access to this resource isn't allowed" 403))

(defn wrap-json-authenticate
  [handler]
  (friend/authenticate handler
                       {:login-uri "/login"
                        :default-landing-uri "/login"
                        :unauthorized-handler unauthorized-handler
                        :unauthenticated-handler unauthenticated-handler
                        :redirect-on-auth? false
                        :workflows [(json-auth/json-login
                                     :login-uri "/login"
                                     :login-failure-handler json-auth/login-failed
                                     :credential-fn (partial creds/bcrypt-credential-fn users))]}))

(defn wrap-same-user
  [handler user]
  (fn [req]
    (let [auth-user (:identity (friend/current-authentication req))]
      (if (= user auth-user)
        (handler req)
        (error-response "You may not access a different user's resources" 400)))))
