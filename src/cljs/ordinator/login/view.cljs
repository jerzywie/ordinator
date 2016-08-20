(ns cljs.ordinator.login.view
  (:require [petrol.core :refer [send! send-value!]]
            [clojure.string :as s]
            [cljs.ordinator.login.messages :as m]))

(defn user-status-widget
  "Widget to display user status and provide logout link
   for use in header bar navigation"
  [ui-channel app]
  (when-let [username (get-in app [:user :username])]
    [:ul.nav.navbar-nav.navbar-right
     [:li.dropdown
      [:a.dropdown-toggle {:href "#" :data-toggle "dropdown" :role "button" :aria-haspopup "true" :aria-expanded "false"}
       username [:span.caret]]
      [:ul.dropdown-menu
       [:li
        [:a {:href "#/" :on-click (send! ui-channel (m/->DoLogout))} "sign-out"]]]]]))

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
            :value value
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
  [input-element
   ui-channel
   "password"
   "password"
   "password"
   password
   m/->ChangePassword])

(defn submit-login
  [ui-channel id title enabled]
  [:div.orderinput.ordersubmit
   [:input.submit {:type "submit"
                   :id id
                   :value title
                   :disabled (not enabled)
                   :on-click (send! ui-channel (m/->DoLogin))}]])

(defn root
  [ui-channel {:keys [username password] :as app}]
  (let [submit-enabled (not (or (nil? (seq username)) (nil? (seq password))))]
    [:div
     [:div
      [:h2 "Please sign-in"]
      [username-input ui-channel username]
      [password-input ui-channel password]
      [submit-login ui-channel "login" "Sign in" submit-enabled]]
     [:div.errormessage
      [:span (:message app)]]]))
