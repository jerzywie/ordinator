(ns cljs.ordinator.order.view
  (:require [petrol.core :refer [send! send-value!]]
            [cljs.ordinator.order.messages :as m]
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

(defn code->key
  [code]
  (-> code s/trim s/lower-case keyword))

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



(defn render-order-line [{:keys [code origin description packsize price unit quantity estcost]}]
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
    [:button.destroy {:on-click nil}]]
   ])

(defn render-order [items]
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
          (map render-order-line (vals items)))]])

(defn root [ui-channel app]
  (let [username "jerzy"]
    (fn [ui-channel app]
      ;;(get-catalogue catalog)
      [:div
       [:div
        [:div [:h2 "Your current order"]
         [:div [:h3 "Enter new item"]
          ;;[order-item-component]
          ]]
        [:div
         [:h3 "Items"]
         ;;[submit-save-order]
         [render-order (:items app)]
         ]]])))
