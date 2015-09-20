(ns ordinator.utils
  (:require [ordinator.login :as login]))

(defn header []
  [:div.header
   [:h1 [:div.site-title "Albany ordinator"]]
   [:span (login/get-username)]])
