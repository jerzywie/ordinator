(ns ordinator.login
  (:require [ordinator.utils :as utils]
            [reagent.core :as r]
            [cljs.core.async :refer [chan <! close!]]
            [cljs-http.client :as http]
            [clojure.string :as s])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def username (r/atom nil))
(def password (r/atom nil))
(def message (r/atom nil))

(def user (r/atom nil))

(defn logged-in?
  []
  (not (nil? @user)))

(defn get-username
  []
  (when (logged-in?)
    (:username @user)))

(defn has-role?
  [role]
  (when (logged-in?)
    (some #{role} (:roles @user))))

(defn do-login!
  []
  (go
    (let [{:keys [status body] :as response} (<! (http/post
                                                  "/login"
                                                  {:json-params
                                                   {:username @username :password @password}}))]
      (case status
        201 (reset! user body)
        (reset! message (:reason body)))
      (reset! password nil))))

(defn input-element
  "An input element which updates its value on change."
  [id name type value]
  [:div
   [:label {:for id} (s/capitalize name)]
   [:input {:id id
            :name name
            :class "form-control"
            :type type
            :required ""
            :value @value
            :on-change (fn [e] (reset! value (-> e .-target .-value)))}]])

(defn submit-login
  [id title enabled on-click]
  [:div.orderinput.ordersubmit
   [:input.submit {:type "submit"
                   :id id
                   :value title
                   :disabled (not enabled)
                   :on-click (fn [e] (if on-click (on-click)))}]])

(defn username-input
  [username]
  (fn []
   [input-element "username" "username" "text" username]))

(defn password-input
  [password]
  (fn []
   [input-element "password" "password" "password" password]))

(defn sign-out! []
  (go
    (let [{:keys [status body] :as response} (<! (http/delete "/login" ))]
      (case status
        200 (reset! user nil)))))

(defn header []
  [:div.header
   [:h1.site-title "Albany ordinator"]
   (when-let [username (get-username)]
     [:div.username
      [:div username]
      [:div [:a {:href "#/" :on-click (fn [e]  (sign-out!))} "sign-out"]]])])

(defn render-login-page []
  (let [submit-enabled (not (and (nil? @username) (nil? @password)))]
    [:div
     [:div
      [header]
      [:h2 "Please sign-in"]
      [username-input username]
      [password-input password]
      [submit-login "login" "Sign in" submit-enabled do-login!]]
     [:div.errormessage
      [:span @message]]]))
