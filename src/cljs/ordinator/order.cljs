(ns ordinator.order
  (:require [reagent.core :as r]))

(defn tocurrency
  "Format a number for currency display (2 digits. Parentheses when negative)"
  [v]
  (double v))

(def catalog (r/atom {}))

(def member-order (r/atom [{:code "X666P" :description "loo-rolls" :case-size "36x1" :unit-cost 8.43 :memdes 36 :memcost 8.43}
                           {:code "V445P" :description "Chopped tinned tomatoes" :case-size "12x400g" :unit-cost 7.56 :memdes 24 :memcost 15.12}]))

(defn add-order-line [])

(defn render-order-line [{:keys [code description case-size unit-cost memdes memcost]}]
  [:tr
   [:td code]
   [:td description]
   [:td case-size]
   [:td.rightjust (tocurrency unit-cost)]
   [:td.rightjust memdes]
   [:td.rightjust (tocurrency memcost)]
   ])

(defn render-totals [])


(defn render-order []
  [:div
   [:table.order
    [:thead
     [:tr
      [:th "Code"]
      [:th "Item"]
      [:th "Unit Q'ty"]
      [:th "Unit Price"]
      [:th "Pref Q'ty"]
      [:th "Est price"]]]
    (into [:tbody]
          (for [line @member-order]
            [render-order-line line]))]])
