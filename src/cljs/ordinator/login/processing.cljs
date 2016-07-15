(ns cljs.ordinator.login.processing
  (:require [petrol.core :refer [Message EventSource]]
            [cljs.ordinator.login.messages :as m]
            [cljs.ordinator.login.rest :as rest]))

(extend-protocol Message
  m/ChangeUsername
  (process-message [{:keys [username]} app]
    (assoc app :username username)))

(extend-protocol Message
  m/ChangePassword
  (process-message [{:keys [password]} app]
    (assoc app :password password)))

(extend-protocol Message
  m/DoLogin
  (process-message [_ app] app))

(extend-protocol EventSource
  m/DoLogin
  (watch-channels [_ {:keys [username password] :as app}]
    #{(rest/do-login username password)}))

(extend-protocol Message
  m/LoginResult
  (process-message [{:keys [status body] :as response} app]
    (case status
      201 (assoc app :user body :message nil :loggedin true)
      (assoc app :user nil :message (:reason body) :loggedin false))))
