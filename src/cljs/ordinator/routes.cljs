(ns cljs.ordinator.routes
  (:require [cljs.core.async :refer [to-chan]]
            [petrol.core :refer [Message process-message EventSource]]
            [petrol.routing :refer [UrlHistoryEvent]]
            [cljs.ordinator.messages :as m]))


(def app-routes ["" {"#/" {""          :home-page
                          "about"      :about-page
                          "login"      :login-page
                          "order"      :order-page
                          "allorders"  :allorders-page}}])

(extend-protocol Message

  UrlHistoryEvent
  (process-message [{view :view} app]
    (let [loggedin (get-in app [:login :loggedin])
          loginhandler {:handler :login-page}]
      (if loggedin
        (assoc app :view view)
        (assoc app :view loginhandler)))))




(defn href-for
  ([handler]
   (href-for handler {}))
  ([handler args]
   (petrol.routing/href-for app-routes handler args)))
