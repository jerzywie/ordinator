(ns cljs.ordinator.view
  (:require [petrol.core :refer [send! forward]]
            [cljs.pprint :refer [pprint]]
            [ordinator.utils :as utils]
            [cljs.ordinator.routes :refer [href-for]]
            [cljs.ordinator.messages :as m]
            [cljs.ordinator.login.messages :as login-messages :refer [DoLogout]]
            [cljs.ordinator.login.view :as login-view]
            [cljs.ordinator.order.view :as order-view]
            [cljs.ordinator.allorders.view :as allorders-view]))

(defn sign-out! [])

(defn home-page []
  (fn [] [:div "home-page"]))

(defn about-page []
  (fn [] [:div "about-page"]))

(defn order-page [ui-channel app]
  [:div
   [order-view/root (forward m/->Order ui-channel) (:order app)]])

(defn allorders-page [ui-channel app]
  [:div
   [allorders-view/root (forward m/->AllOrders ui-channel) (:allorders app)]])

(defn login-page [ui-channel app]
  [:div
   [login-view/root (forward m/->Login ui-channel) (:login app)]])

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
      ;[:li [:a {:href (href-for :login-page)} "Login"]]
      ]
     [login-view/user-status-widget (forward m/->Login ui-channel) (:login app)]]]])

(defn root [ui-channel app]
  [:div
   [header ui-channel app]
   [:div
    (case (-> app :view :handler)
      :about-page [about-page]
      :order-page [order-page ui-channel app]
      :allorders-page [allorders-page ui-channel app]
      :login-page [login-page ui-channel app]
      [home-page ui-channel app])]
   [:h4.clear-left "debug app state"]
   [:pre.font-size-xsmall (with-out-str (pprint app))]])
