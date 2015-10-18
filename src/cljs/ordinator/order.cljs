(ns ordinator.order
  (:require [ordinator.login :as login]
            [ordinator.utils :as utils]
            [reagent.core :as r]
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

(defn cache-order
  "Get order and save in local cache"
  [user orderdate]
  (utils/get-order! user orderdate)
  (prn "cache-order " (utils/get-order-items)))

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

(defn get-catalogue [result]
  "Get catalogue data.
   result is the ratom to hold the data"
  (go
    (when (nil? (seq @result))
      (reset! result {:loading true})
      (prn "getting catalogue")
      (let [{:keys [status body] :as response} (<! (http/get "/catalogue"))]
        (reset! result body)))))

(defn order-input-field
  [id placeholder value on-change]
  [:div.orderinput
   [:label {:for id} (s/capitalize id)]
   [:input {:type "text"
            :id id
            :placeholder placeholder
            :value value
            :on-change (fn [e]
                         (let [val (.-target.value e)]
                           (if on-change (on-change val))))}]])

(defn order-readonly-field
  [id title value]
  [:div.orderinput
   [:label {:for id} title]
   [:span.orderinput value]])

(defn add-order-line
  [id title enabled on-click]
  [:div.orderinput
   [:input.submit {:type "submit"
                   :id id
                   :value title
                   :disabled (not enabled)
                   :on-click (fn [e] (if on-click (on-click)))}]])

(defn render-order-line [{:keys [code origin description packsize price unit quantity estcost]}]
  [:tr
   [:td code]
   [:td (s/trim (str origin " " description))]
   [:td packsize]
   [:td.rightjust (tonumber price)]
   [:td unit]
   [:td.rightjust quantity]
   [:td.rightjust (tonumber estcost)]
   [:td
    [:button.destroy {:on-click #(utils/delete-order-item (code->key code))}]]
   ])

(defn render-totals [])


(defn render-order []
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
          (map render-order-line (vals (utils/get-order-items))))]])

(defn calc-order-item
  []
  (let [{:keys [code description origin packsize price vat unit unitsperpack splits? quantity estcost]
         :as order-line} @order-item]
    (when (and (> price 0) (nil? estcost))
      (assoc order-line :estcost (* (/ quantity unitsperpack) price)))
    order-line))

(defn order-item-component
  []
  (let [{:keys [code codeval description origin packsize price vat
                unit unitsperpack splits? quantity estcost]
         :as order-line} (calc-order-item)
         submit-enabled (and (> price 0) (> quantity 0))
         code-onchange (fn [codeval]
                         (let [codekey (code->key codeval)
                               itemdata (assoc (codekey @catalog) :estcost nil :quantity nil :codeval codeval)]
                           (reset! order-item itemdata)
                           (prn "code o/c" @order-item)))
         quantity-onchange (fn [qty] (let [estcost (* (/ qty unitsperpack) price)]
                                       (swap! order-item assoc :quantity qty :estcost estcost)
                                       (prn "qty o/c" @order-item)))
         add-onclick (fn [] (let [code (code->key (:code @order-item))]
                              (utils/add-order-item code @order-item)
                              (reset! order-item nil)))]

    [:div.container
     [:div.clearfix
      [:span
       [order-input-field "code" "code?" codeval code-onchange]
       [order-readonly-field "packsize" "Pack size" packsize]
       [order-readonly-field "packprice" "Pack price" (str (toprice price) (addvat vat))]
       [order-readonly-field "unit" "Albany unit" unit]
       [order-readonly-field "unisperpack" "Units/pack" unitsperpack]
       [order-readonly-field "unitprice" "Price/unit" (toprice (/ price unitsperpack))]
       [order-input-field "quantity" "in units" quantity quantity-onchange]
       [order-readonly-field "packsordered" "Packs ordered" (tonumber (/ quantity unitsperpack))]
       [order-readonly-field "estcost" "Estimated cost" (toprice estcost)]
       [add-order-line "add" "Add" submit-enabled add-onclick]]]
     [:div.clearfix
      [:span
       [:div.orderinput
        [:span.origin.orderinput origin]
        [:span.description.orderinput description]]]]]))

(defn submit-save-order
  []
  [:div.orderinput.ordersubmit
   [:input.submit {:type "submit"
                   :id "saveorder"
                   :value "Save"
                   :on-click (fn [e] (utils/save-order))}]])

(defn render-order-page []
  (get-catalogue catalog)
  (let [username (utils/get-username)]
    (cache-order username "current")
    [:div
     [login/header]
     [:div
      [:div [:h2 "Your current order"]
       [:div [:h3 "Enter new item"]
        [order-item-component]]]
      [:div
       [:h3 "Items"]
       [submit-save-order]
       [render-order]]
      [:div [:a {:href "#/"} "go to the home page"]]]]))
