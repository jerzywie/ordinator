(ns ordinator.utils
  (:require [ordinator.login :as login]))

(defn header []
  [:div.header
   [:h1.site-title "Albany ordinator"]
   [:div.username (login/get-username)]])
