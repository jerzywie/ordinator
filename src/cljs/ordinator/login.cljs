(ns ordinator.login
  (:require [ordinator.utils :as utils]
            [reagent.core :as r]
            [cljs.core.async :refer [chan <! close!]]
            [cljs-http.client :as http]
            [clojure.string :as s])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def username (r/atom nil))
(def password (r/atom nil))

(defn do-login
  []
  (utils/do-login! @username @password)
  (reset! password nil))

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
        200 (utils/reset-appstate!)))))

(defn header []
  [:nav.navbar.navbar-default.navbar-fixed-top
   [:div.container-fluid
    [:div.navbar-header
     [:span.navbar-brand.site-title "Albany ordinator"]]
    [:div.collapse.navbar-collapse
     [:ul.nav.navbar-nav
      [:li [:a {:href "#/order"} "View your order"]]
      [:li [:a {:href "#/about"} "About"]]
      [:li [:a {:href "#/allorders"} "View collated order"]]]
     (when-let [username (utils/get-username)]
       [:ul.nav.navbar-nav.navbar-right
        [:li.dropdown
         [:a.dropdown-toggle {:href "#" :data-toggle "dropdown" :role "button" :aria-haspopup "true" :aria-expanded "false"}
          username [:span.caret]]
         [:ul.dropdown-menu
          [:li
           [:a {:href "#/" :on-click (fn [e]  (sign-out!))} "sign-out"]]]]])]]])

(defn render-login-page []
  (let [submit-enabled (not (and (nil? @username) (nil? @password)))]
    [:div
     [:div
      [header]
      [:h2 "Please sign-in"]
      [username-input username]
      [password-input password]
      [submit-login "login" "Sign in" submit-enabled do-login]]
     [:div.errormessage
      [:span (utils/get-message)]]]))
