(ns cljs.ordinator.order.core
  (:require [reagent.core :as reagent]))

(def initial-state nil)

(defonce !app
  (reagent/atom initial-state))
