(ns cljs.ordinator.login.core
  (:require [reagent.core :as reagent :refer [atom]]))

(def initial-state {:username nil
                    :password nil})

(defonce !app
  (reagent/atom initial-state))
