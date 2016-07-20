(ns cljs.ordinator.order.view
  (:require [petrol.core :refer [send! send-value!]]
            [cljs.ordinator.order.messages :as m]
            [ordinator.utils :as u]
            [reagent.core :as reagent]
            [clojure.string :as s]
            [goog.string :as gstring]
            [goog.string.format :as gformat]))

(defn tocurrency
  "Format a number for currency display (2 digits. Parentheses when negative)"
  [v]
  (double v))

;;TODO get rid of this state
(def order-item (reagent/atom nil))


;TODO move these and the dupes in collation.cljs into utils
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

(defn addvat [v]
  (if (= v "Z") " +VAT" ""))



(defn render-order-line
  [ui-channel {:keys [code origin description packsize price unit quantity estcost]}]
  [:tr {:id code
        :on-click nil}
   [:td code]
   [:td (s/trim (str origin " " description))]
   [:td packsize]
   [:td.rightjust (tonumber price)]
   [:td unit]
   [:td.rightjust quantity]
   [:td.rightjust (tonumber estcost)]
   [:td
    [:button.destroy {:on-click (send! ui-channel (m/->DeleteOrderLine (u/code->key code)))}]]])

(defn render-order
  [ui-channel items]
  [:div
   [:table.order
    [:thead
     [:tr
      [:th "Code"]
      [:th "Item"]
      [:th "Pack size"]
      [:th "Pack Price"]
      [:th "Albany Unit"]
      [:th "Quantity"]
      [:th "Est cost"]
      [:th ""]]]
    (into [:tbody]
          (map (partial render-order-line ui-channel) (vals items)))]])

(defn order-input-field
  [ui-channel id placeholder value on-change-message]
  [:div.orderinput
   [:label {:for id} (s/capitalize id)]
   [:input {:type "text"
            :id id
            :placeholder placeholder
            :value value
            :on-change (send-value! ui-channel on-change-message)}]])

(defn order-readonly-field
  [id title value]
  [:div.orderinput
   [:label {:for id} title]
   [:span.orderinput value]])

(defn add-order-line
  [ui-channel app id title enabled]
  [:div.orderinput
   [:input.submit {:type "submit"
                   :id id
                   :value title
                   :disabled (not enabled)
                   :on-click (send! ui-channel (m/->AddItem))}]])

(defn order-entry-form
  [ui-channel app]
  (let [{:keys [code codestr description origin packsize price vat
                unit unitsperpack splits? quantity estcost]} (:order-item app)
                submit-enabled (and (> price 0) (> quantity 0))]
    (prn "quantity" quantity)
    [:div.order-container
     [:div.clearfix
      [:span
       [order-input-field ui-channel "code" "code?" codestr m/->ChangeCode]
       [order-readonly-field "packsize" "Pack size" packsize]
       [order-readonly-field "packprice" "Pack price" (str (toprice price) (addvat vat))]
       [order-readonly-field "unit" "Albany unit" unit]
       [order-readonly-field "unisperpack" "Units/pack" unitsperpack]
       [order-readonly-field "unitprice" "Price/unit" (toprice (/ price unitsperpack))]
       [order-input-field ui-channel "quantity" "in units" quantity m/->ChangeQuantity]
       [order-readonly-field "packsordered" "Packs ordered" (tonumber (/ quantity unitsperpack))]
       [order-readonly-field "estcost" "Estimated cost" (toprice estcost)]
       [add-order-line ui-channel app "add" "Add" submit-enabled]]]
     [:div.clearfix
      [:span
       [:div.orderinput
        [:span.origin.orderinput origin]
        [:span.description.orderinput description]]]]]))

(defn root
  [ui-channel app]
  (let [username "jerzy"]
    (fn [ui-channel app]
      [:div
       [:div
        [:div [:h2 "Your current order"]
         [:div [:h3 "Enter new item"]
          [order-entry-form ui-channel app]]]
        [:div
         [:h3 "Items"]
         ;;[submit-save-order]
         [render-order ui-channel (:items app)]
         ]]])))
