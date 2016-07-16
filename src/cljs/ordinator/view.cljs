(ns cljs.ordinator.view
  (:require [petrol.core :refer [send! forward]]
            [ordinator.utils :as utils]
            [cljs.ordinator.routes :refer [href-for]]
            [cljs.ordinator.messages :as m]
            [cljs.ordinator.login.view :as login]
            [cljs.ordinator.login.messages :as login-messages :refer [DoLogout]]))

(defn sign-out! [])

(defn home-page []
  (fn [] [:div "home-page"]))

(defn about-page []
  (fn [] [:div "about-page"]))

(defn order-page []
  (fn [] [:div "order-page"]))

(defn allorders-page []
  (fn [] [:div "all-orders-page"]))

(defn login-page [ui-channel app]
  [:div
   [login/root (forward m/->Login ui-channel) (:login app)]])

(defn header [ui-channel app]
  [:nav.navbar.navbar-default.navbar-fixed-top
   [:div.container-fluid
    [:div.navbar-header
     [:span.navbar-brand.site-title "Albany ordinator"]]
    [:div.collapse.navbar-collapse
     [:ul.nav.navbar-nav
      [:li [:a {:href (href-for :order-page)} "View your order"]]
      [:li [:a {:href (href-for :about-page)} "About"]]
      [:li [:a {:href (href-for :allorders-page)} "View collated order"]]
      [:li [:a {:href (href-for :login-page)} "Login"]]]
     [login/user-status-widget (forward m/->Login ui-channel) (:login app)]]]])

(defn root [ui-channel app]
  [:div
   [header ui-channel app]
   (case (-> app :view :handler)
     :about-page [about-page]
     :order-page [order-page]
     :allorders-page [allorders-page]
     :login-page [login-page ui-channel app]
     [home-page ui-channel app])
   [:h3 "debug app state"]
   [:div [:code (pr-str app)]]])
