(ns cljs.ordinator.routes
  (:require [petrol.core :refer [Message process-message]]
            [petrol.routing :refer [UrlHistoryEvent]]))


(def app-routes ["" {"#/" {""           :home-page
                          "about"      :about-page
                          "login"      :login-page
                          "order"      :order-page
                          "allorders"  :allorders-page}}])

(extend-protocol Message
  UrlHistoryEvent
  (process-message [{view :view} app]
    (assoc app :view view)))

(defn href-for
  ([handler]
   (href-for handler {}))
  ([handler args]
   (petrol.routing/href-for app-routes handler args)))
