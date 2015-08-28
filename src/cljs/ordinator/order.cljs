(ns ordinator.order
  (:require [reagent.core :as r]
            [cljs.core.async :refer [chan <! close!]]
            [cljs-http.client :as http])
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

(defn order-input
  [{:keys [id placeholder val]}]
  (let [changed "blah"]
   (fn [props]
      [:div
       [:label {:for id} id]
       [:input (merge props
                      {:type "text"
                       :placeholder placeholder
                       :value @val
                       :on-change #(reset! val (-> % .-target .-value))
                       :on-blur (prn id ": " @val)})]])))


(defn order-line-input
  []
  (let [code (r/atom "")
        quantity (r/atom 0)
        description (r/atom "")
        ]
   [:div
    [order-input {:id "code"
                  :placeholder "code?"
                  :val code
                  }]
    [order-input {:id "quantity"
                  :placeholder "albany units"
                  :val quantity}]]
   [:div
    [:span @code]]))

(defn order-input-field
  [])

(defn order-item-component
  []
  (let [{:keys [code description price origin packsize vat albanypack splits? quantity estcost]} @order-item]
    ))

(defn render-order-page []
  (get-catalogue! catalog)
  [:div
   [:div [:h2 "Your current order"]
    [:div [:h3 "Enter new item"]
     [order-line-input]]]
   [:div
    [:h3 "Items"]
    [render-order]]
   [:div [:a {:href "#/"} "go to the home page"]]])
