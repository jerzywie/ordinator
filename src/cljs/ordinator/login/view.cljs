(ns cljs.ordinator.login.view
  (:require [petrol.core :refer [send! send-value!]]
            [clojure.string :as s]
            [cljs.ordinator.login.messages :as m]))

(defn input-element
  "An input element which updates its value on change."
  [ui-channel id name type value change-fn]
  [:div
   [:label {:for id} (s/capitalize name)]
   [:input {:id id
            :name name
            :class "form-control"
            :type type
            :required ""
            :defaultValue value
            :on-change (send-value! ui-channel change-fn)}]])

(defn username-input
  [ui-channel username]
  [input-element
   ui-channel
   "username"
   "username"
   "text"
   username
   m/->ChangeUsername])

(defn password-input
  [ui-channel password]
  (fn []
    [input-element
     ui-channel
     "password"
     "password"
     "password"
     password
     m/->ChangePassword]))

(defn submit-login
  [ui-channel id title enabled]
  [:div.orderinput.ordersubmit
   [:input.submit {:type "submit"
                   :id id
                   :value title
                   :disabled (not enabled)
                   :on-click (send! ui-channel m/->DoLogin)}]])

(defn root
  [ui-channel {:keys [username password] :as app}]
  (let [submit-enabled (not (or (nil? username) (nil? password)))]
    (prn "login:root u" username "p" password "app" app "submit-enabled" submit-enabled)
    [:div
     [:div
      [:h2 "Please sign-in"]
      [username-input ui-channel username]
      [password-input ui-channel password]
      [submit-login ui-channel "login" "Sign in" submit-enabled]]
     [:div.errormessage
      [:span (:message app)]]]))
