(ns ordinator.tablecell
  (:require [reagent.core :as reagent]))

(defn make-key
  [code pers]
  (str code "-" pers))

(defn cell-input
  [{:keys [key value class on-save on-stop]}]
  (let [value (reagent/atom value)
        stop #(if on-stop (on-stop))
        save #(if on-save (on-save))]
    (fn [props]
      [:input {:type "text"
               :class (str (:class props))
               :value @value
               :on-blur save
               :on-change #(reset! value (-> % .-target .-value))
               :on-key-down #(case (.-which %)
                               13 (save)
                               27 (stop)
                               nil)}])))

(def cell-input-give-focus (with-meta cell-input
                             {:component-did-mount #(.focus (reagent/dom-node %))}))

(defn render-cell
  [{:keys [key value class]}]
  [:td {:class class} value])

(defn render-editable-cell
  [{:keys [key value class editingfn givefocus? on-stop on-save]}]
  (let [classval (str class (if (editingfn) " edit-hide"))
        renderfn (if givefocus? cell-input-give-focus cell-input)]
    [:td
     [:span {:class classval} value]
     (when (editingfn)
       [renderfn {:class (str class " edit")
                  :value value
                  :on-stop on-stop
                  :on-save on-save}])]))
