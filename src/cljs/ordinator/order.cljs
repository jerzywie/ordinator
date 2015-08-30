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

(def member-order (r/atom []))


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

(defn get-catalogue! [result]
  "Get catalogue data.
   result is the ratom to hold the data"
  (go
    (reset! result {:loading true})
    (let [{:keys [status body] :as response} (<! (http/get "/catalogue"))]
      (reset! result body))))

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
   [:span.orderinput value]])

(defn submit-order
  [id title enabled on-click]
  [:div.orderinput.ordersubmit
   [:input.submit {:type "submit"
                   :id id
                   :value title
                   :disabled (not enabled)
                   :on-click (fn [e] (if on-click (on-click)))}]])

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

(defn calc-order-item
  []
  (let [{:keys [code description origin packsize price vat unit unitsperpack splits? quantity estcost]
         :as order-line} @order-item]
    (when (and (> price 0) (nil? estcost))
      (assoc order-line :estcost (* (/ quantity unitsperpack) price)))
    order-line))

(defn order-item-component
  []
  (let [{:keys [code description origin packsize price vat unit unitsperpack splits? quantity estcost]
         :as order-line} (calc-order-item)
         submit-enabled (and (> 0 price) (> 0 quantity))
         code-onchange (fn [code] (let [codekey (-> code s/trim s/lower-case keyword)
                                       itemdata (assoc (codekey @catalog) :estcost nil :quantity nil)]
                                   (reset! order-item itemdata)
                                   (prn "code o/c" @order-item)))
         quantity-onchange (fn [qty] (let [estcost (* (/ qty unitsperpack) price)]
                                      (swap! order-item assoc :quantity qty :estcost estcost)
                                      (prn "qty o/c" @order-item)))
         add-onclick (fn [] (let [result (swap! member-order conj @order-item)]
                             (prn "add o/c" result)))]
    (prn "o-i-c!")
    [:div.container
     [:div.clearfix
      [:span
       [order-input-field "code" "code?" code-onchange]
       [order-readonly-field "packsize" "Pack size" packsize]
       [order-readonly-field "packprice" "Pack price" (str (toprice price) (addvat vat))]
       [order-readonly-field "unit" "Albany unit" unit]
       [order-readonly-field "unisperpack" "Units/pack" unitsperpack]
       [order-readonly-field "unitprice" "Price/unit" (toprice (/ price unitsperpack))]
       [order-input-field "quantity" "in units" quantity-onchange]
       [order-readonly-field "packsordered" "Packs ordered" (tonumber (/ quantity unitsperpack))]
       [order-readonly-field "estcost" "Estimated cost" (toprice estcost)]
       [submit-order "add" "Add" (and (> price 0) (> quantity 0)) submit-enabled add-onclick]]]
     [:div.clearfix
      [:span
       [:div.orderinput
        [:span.origin.orderinput origin]
        [:span.description.orderinput description]]]]]))


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
