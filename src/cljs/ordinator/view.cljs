(ns cljs.ordinator.view
  (:require [ordinator.utils :as utils]
            [cljs.ordinator.routes :refer [href-for]]))

(defn sign-out! [])

(defn home-page []
  (fn [] [:div "home-page"]))

(defn about-page []
  (fn [] [:div "about-page"]))

(defn order-page []
  (fn [] [:div "order-page"]))

(defn allorders-page []
  (fn [] [:div "all-orders-page"]))

(defn header [ui-channel app]
  [:nav.navbar.navbar-default.navbar-fixed-top
   [:div.container-fluid
    [:div.navbar-header
     [:span.navbar-brand.site-title "Albany ordinator"]]
    [:div.collapse.navbar-collapse
     [:ul.nav.navbar-nav
      [:li [:a {:href (href-for :order-page)} "View your order"]]
      [:li [:a {:href (href-for :about-page)} "About"]]
      [:li [:a {:href (href-for :allorders-page)} "View collated order"]]]
     (when-let [username (utils/get-username)]
       [:ul.nav.navbar-nav.navbar-right
        [:li.dropdown
         [:a.dropdown-toggle {:href "#" :data-toggle "dropdown" :role "button" :aria-haspopup "true" :aria-expanded "false"}
          username [:span.caret]]
         [:ul.dropdown-menu
          [:li
           [:a {:href "#/" :on-click (fn [e]  (sign-out!))} "sign-out"]]]]])]]])

(defn root [ui-channel app]
  [:div
   [header ui-channel app]
   [(case (-> app :view :handler)
      :about-page (@(var about-page))
      :order-page (@(var order-page))
      :allorders-page (@(var allorders-page))
      (@(var home-page) ui-channel app))]
   [:h3 "debug app state"]
   [:div [:code (pr-str app)]]])
