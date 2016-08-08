(ns cljs.ordinator.allorders.view
  (:require [petrol.core :refer [send! send-value!]]
            [cljs.ordinator.allorders.messages :as m]
            [cljs.ordinator.allorders.tablecell :as tablecell]
            [ordinator.utils :as u]))

(defn make-key
  [code member]
  (str code "-" (name member)))

(defn render-order-details
  [ui-channel app code member focus {:keys [quantity estcost]}]
  [tablecell/render-table-cell {:key member
                                   :ui-channel ui-channel
                                   :value quantity
                                   :class "qty"
                                   :iseditable? (= (:editing app) (u/code->key code))
                                   :givefocus? focus}])

(defn render-collation-line
  [ui-channel app [key {:keys [itemdata orders]}]]
  (let [{:keys [code description packsize price unit]} itemdata
        members [:jerzy :sally :matthew]]
    [:tr {:id code
          :on-double-click (send! ui-channel (m/->EditItem (u/code->key code)))}
     [:td code]
     [:td description]
     [:td packsize]
     [:td.rightjust (u/tonumber price)]
     [:td unit]
     (map #(render-order-details ui-channel app code %1 (= 0 %2) (%1 orders)) members (range))
     [:td
      [:span
       [:button.edit "edit"]
       " "
       [:button.save "save"]
       " "
       [:button.delete "delete"]]]]))

(defn render-collated-order
  [ui-channel {:keys [items] :as app}]
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
          (map (partial render-collation-line ui-channel app) items))]])

(defn root
  [ui-channel app]
  (fn [ui-channel app]
    [:div
     [:div
      [:div [:h2 "Entire collated order"]]
      [:div
       [render-collated-order ui-channel app]]]]))
