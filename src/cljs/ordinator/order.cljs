(ns ordinator.order
  (:require [reagent.core :as r]
            [cljs.core.async :refer [chan <! close!]]
            [cljs-http.client :as http]
            [clojure.string :as s]
            [goog.string :as gstring]
            [goog.string.format :as gformat])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn tocurrency
  "Format a number for currency display (2 digits. Parentheses when negative)"
  [v]
  (double v))

(def catalog (r/atom nil))

(def order-item (r/atom nil))

(def member-order (r/atom [{:code "X666P"
                            :description "loo-rolls"
                            :case-size "36x1"
                            :unit-cost 8.43
                            :memdes 36
                            :memcost 8.43}
                           {:code "V445P"
                            :description "Chopped tinned tomatoes"
                            :case-size "12x400g"
                            :unit-cost 7.56
                            :memdes 24
                            :memcost 15.12}]))


(defn get-catalogue! [result]
  "Get catalogue data.
   result is the ratom to hold the data"
  (go
    (reset! result {:loading true})
    (let [{:keys [status body] :as response} (<! (http/get "/catalogue"))]
      (reset! result body))))

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

(defn order-input-field
  [id placeholder on-change]
  [:div.orderinput
   [:label {:for id} (s/capitalize id)]
   [:input {:type "text"
            :id id
            :placeholder placeholder
            :on-change (fn [e]
                         (let [val (.-target.value e)]
                           (if on-change (on-change val))))}]])

(defn order-readonly-field
  [id title value]
  [:div.orderinput
   [:label {:for id} title]
   [:span value]])

(defn toprice [p]
  (cond
   (nil? p) ""
   (js/isNaN p) ""
   p (gstring/format "£%0.2f" p)
   :else ""))

(defn addvat [v]
  (if (= v "Z") " +VAT" ""))

(defn order-item-component
  []
  (let [{:keys [code description origin packsize price vat unit unitsperpack splits? quantity estcost]
         :as order-line} @order-item
         code-onchange (fn [code] (let [codekey (-> code s/trim s/lower-case keyword)
                                       itemdata (codekey @catalog)]
                                   (reset! order-item itemdata)))
         quantity-onchange (fn [qty] (let [estcost (* (/ qty unitsperpack) price)]
                                      (swap! order-item assoc :quantity qty :estcost estcost)
                                      (prn @order-item)))]
    [:div.container
     [:div.clearfix
      [:span
       [order-input-field "code" "code?" code-onchange]
       [order-readonly-field "packsize" "Pack size" packsize]
       [order-readonly-field "unit" "Albany unit" unit]
       [order-readonly-field "unisperpack" "Units/pack" unitsperpack]
       [order-readonly-field "unitprice" "Price/unit" (toprice (/ price unitsperpack))]
       [order-input-field "quantity" "quantity in Albany units" quantity-onchange]
       [order-readonly-field "estcost" "Estimated cost" (toprice estcost)]]]
     [:div.clearfix
      [:span
       [:div.orderinput
        [:div origin]
        [:div description]]
       [order-readonly-field "packprice" "Pack price" (str (toprice price) (addvat vat))]]]]))


(defn render-order-page []
  (get-catalogue! catalog)
  [:div
   [:div [:h2 "Your current order"]
    [:div [:h3 "Enter new item"]
     [order-item-component]]]
   [:div
    [:h3 "Items"]
    [render-order]]
   [:div [:a {:href "#/"} "go to the home page"]]])
