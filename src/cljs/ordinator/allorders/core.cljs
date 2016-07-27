(ns cljs.ordinator.allorders.core
  (:require [reagent.core :as reagent]))

(def initial-state {:editing nil})

(defonce !app
  (reagent/atom initial-state))
