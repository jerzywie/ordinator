(ns cljs.ordinator.allorders.view
  (:require [petrol.core :refer [send! send-value!]]
            [cljs.ordinator.allorders.messages :as m]
            [cljs.ordinator.allorders.tablecell :as tablecell]
            [ordinator.utils :as u]))

(defn make-key
  [first second]
  (str (name first) "-" (name second)))

(defn total-quantity
  [orders]
  (->> (vals orders)
       (map :quantity)
       (reduce + 0)))

(defn total-class
  [quantity unitsperpack splits?]
  (if (u/valid-quantity? quantity unitsperpack splits?) "bg-success" "bg-danger"))

(defn action-button
  [ui-channel code value disabled? message]
  [:input.submit {:type "submit"
                  :id (make-key code value)
                  :value value
                  :disabled disabled?
                  :on-click (send! ui-channel (message code))}])

(defn destroy-button
  [ui-channel {:keys [deleteconfirm editing] :as app} code]
  (let [disabled? (u/disable-it editing deleteconfirm code)]
    (if (= deleteconfirm code)
      [:td
       [action-button ui-channel code "Confirm" disabled? m/->ReallyDeleteItem]
       [action-button ui-channel code "Cancel" disabled? m/->CancelDeleteItem]]
      [:td
       [:button.destroy {:disabled disabled?
                         :on-click (send! ui-channel (m/->ConfirmDeleteItem code))}]])))

(defn non-edit-buttons
  [ui-channel {:keys [deleteconfirm editing]} code]
  [:td
   [action-button ui-channel code "Edit" (u/disable-it deleteconfirm editing code) m/->EditItem]])

(defn edit-buttons
  [ui-channel {:keys [deleteconfirm editing]} code]
  [:td
   [action-button ui-channel code "Save" (u/disable-it deleteconfirm editing code) m/->SaveItem]
   [action-button ui-channel code "Revert" (u/disable-it deleteconfirm editing code) m/->RevertEditing]])

(defn render-order-details
  [ui-channel app code member iseditable? focus {:keys [quantity estcost]}]
  (let [estcost (if estcost estcost 0)]
    [tablecell/render-table-cell {:key member
                                  :ui-channel ui-channel
                                  :value quantity
                                  :secondary-value (u/tonumber estcost)
                                  :class "qty"
                                  :iseditable? iseditable?
                                  :givefocus? focus}]))

(defn render-collation-line
  [ui-channel app [code {:keys [itemdata orders]}]]
  (let [codestr (u/key->code code)
        {:keys [description origin packsize price unit vat unitsperpack splits?]} itemdata
        members [:jerzy :sally :matthew]
        editable? (= (:editing app) code)
        total (total-quantity orders)
        total-class (total-class total unitsperpack splits?)]
    [:tr {:id codestr
          :on-double-click (send! ui-channel (m/->EditItem code))}
     [:td codestr]
     [:td
      [:div [:b description]]
      [:div.tablecell-line2 [:i origin]]]
     [:td packsize]
     [:td.rightjust (u/tonumber price)]
     [:td (u/vat-notice vat)]
     [:td unit]
     (map #(render-order-details ui-channel app code %1 editable? (= 0 %2) (%1 orders))
          members (range))
     [:td.rightjust {:class total-class} total]
     (if editable?
       [edit-buttons ui-channel app code]
       [non-edit-buttons ui-channel app code])
     [destroy-button ui-channel app code]]))

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
      [:th "VAT"]
      [:th "Albany Unit"]
      [:th "Jerzy"]
      [:th "Sally"]
      [:th "Matthew"]
      [:th "Total qty"]
      [:th {:colSpan 2} ""]]]
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
