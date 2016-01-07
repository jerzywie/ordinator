(ns ordinator.collation
  (:require [ordinator.login :as login]
            [ordinator.utils :as utils]
            [reagent.core :as r]
            [cljs.core.async :refer [chan <! close!]]
            [cljs-http.client :as http]
            [clojure.string :as s]
            [goog.string :as gstring]
            [goog.string.format :as gformat]))

;TODO move these and the dupes in order.cljs into utils
(defn tonumber
  ([v curr]
    (cond
     (nil? v) ""
     (js/isNaN v) ""
     v (gstring/format "%s%0.2f" curr v)
     :else ""))
  ([v]
   (tonumber v "")))

(defn toprice [p]
  (tonumber p "£"))

(defn make-key
  [code member]
  (str code "-" (name member)))

(defn render-order-details
  [key {:keys [quantity estcost]}]
  (prn "r-o-d q:" quantity "c:" estcost)
  [:td {:key key
        :on-click #(prn "click " key)} quantity])

(defn render-collation-line
  [[key {:keys [itemdata orders]}]]
  (prn "r-c-l c:" key "i:"  itemdata "o:" orders )
  (let [{:keys [code description packsize price unit]} itemdata
        members [:jerzy :sally :matthew]]
    [:tr
     [:td code]
     [:td description]
     [:td packsize]
     [:td.rightjust (tonumber price)]
     [:td unit]
     (map #(render-order-details (make-key code %) (% orders)) members)
     [:td
      [:span
       [:button.edit "edit"]
       " "
       [:button.save "save"]
       " "
       [:button.delete "delete"]]]]))

(defn render-collated-order
  []
  [:div
   [:table.collatedorder.order
    [:thead
     [:tr
      [:th "Code"]
      [:th "Item"]
      [:th "Pack size"]
      [:th "Pack Price"]
      [:th "Albany Unit"]
      [:th "Jerzy"]
      [:th "Sally"]
      [:th "Matthew"]
      [:th ""]]]
    (into [:tbody]
          (map render-collation-line (:items (utils/all-orders))))]])

(defn render-allorders-page []
  (utils/get-allorders! "current")
  (fn []
    [:div
     [login/header]
     [:div
      [:div [:h2 "Entire collated order"]]
      [:div
       [render-collated-order]]
      [:div [:a {:href "#/"} "go to the home page"]]]]))
