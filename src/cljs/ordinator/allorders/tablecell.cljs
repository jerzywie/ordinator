(ns cljs.ordinator.allorders.tablecell
  (:require [reagent.core :as reagent]
            [petrol.core :refer [send! send-value! send-key!]]
            [cljs.ordinator.allorders.messages :as m]))

(defn make-key
  [code pers]
  (str code "-" pers))

(defn cell-input
  [{:keys [ui-channel key value class] :as props}]
  [:input {:type "text"
           :class class
           :value value
           :on-key-down (send-key! ui-channel m/->KeyEvent (fn [c] (some #{c} [27])))
           :on-change (send-value! ui-channel (partial m/->ChangeQuantity key))
           }])

(def cell-input-give-focus (with-meta cell-input
                             {:component-did-mount #(.focus (reagent/dom-node %))}))

(defn render-cell
  [{:keys [key value class]}]
  [:td {:class class} value])

(defn render-editable-cell
  [{:keys [ui-channel key value class editingfn givefocus?]}]
  (let [spanclass (str class (if editingfn " hide"))
        editclass (str class " edit" (if-not editingfn " hide"))
        renderfn (if givefocus? cell-input-give-focus cell-input)]
    [:td {:key key}
     [:span {:class spanclass} value]
     [renderfn {:ui-channel ui-channel
                :class editclass
                :value value
                :key key}]]))
