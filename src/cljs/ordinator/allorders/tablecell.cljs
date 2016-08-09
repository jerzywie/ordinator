(ns cljs.ordinator.allorders.tablecell
  (:require [reagent.core :as reagent]
            [petrol.core :refer [send! send-value! send-key!]]
            [cljs.ordinator.allorders.messages :as m]))

(defn cell-text-input
  [props]
  (reagent/create-class
   {:component-did-mount
    (if (:focus props) #(.select (reagent/dom-node %)) nil)

    :display-name "cell-input-component"

    :reagent-render
    (fn [{:keys [ui-channel key value class]}]
      [:input {:type "text"
               :class class
               :value value
               :on-key-down (send-key! ui-channel m/->KeyEvent (fn [c] (some #{c} [27])))
               :on-change (send-value! ui-channel (partial m/->ChangeQuantity key))}])}))

(defn render-table-cell
  [{:keys [ui-channel key value class iseditable? givefocus?]}]
  (let [spanclass (str class (if iseditable? " hide"))
        editclass (str class " edit")]
    [:td {:key key}
     (if iseditable?
       [cell-text-input {:ui-channel ui-channel
                         :class editclass
                         :value value
                         :key key
                         :focus givefocus?}]
       [:span {:class spanclass} value])]))
