(ns ordinator.auth
  (:require [cemerick.friend :as friend]
            [cemerick.friend
             [workflows :as workflows]
             [credentials :as creds]]
            [marianoguerra.friend-json-workflow :as json-auth]
            [hiccup.core :refer :all]
            [hiccup.page :as h]
            [hiccup.element :as e]
            [radix.error :refer [error-response]]
            [ring.util.response :refer [status response]]))

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

(defn login-form
  []
  (h/html5
    [:head
     [:title "login page"]]
    [:body
     [:div {:class "row"}
      [:div {:class "columns small-12"}
       [:h3 "Login"]
       [:div {:class "row"}
        [:form {:method "POST" :action "login" :class "columns small-4"}
         [:div "Username" [:input {:type "text" :name "username"}]]
         [:div "Password" [:input {:type "password" :name "password"}]]
         [:div [:input {:type "submit" :class "button" :value "Login"}]]]]]]]))

(defn wrap-form-authenticate
  [handler]
  (friend/authenticate handler
                       {:allow-anon? true
                        :credential-fn (partial creds/bcrypt-credential-fn users)
                        :login-uri "/login"
                        :default-landing-uri "/login"
                        :unauthorized-handler #(-> (h/html5
                                                    [:h2 "You do not have sufficient privileges to access " (:uri %)])
                                                   response
                                                   (status 401))
                        :workflows [(workflows/interactive-form)]}))

(defn unauthenticated-handler [thing]
  "handler when authentication fails"
  (error-response "You need to be logged in to use this resource" 401))

(defn unauthorized-handler [thing]
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
