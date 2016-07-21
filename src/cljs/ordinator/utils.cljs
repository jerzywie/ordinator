(ns ordinator.utils
  (:require [reagent.core :as reagent]
            [cljs.core.async :refer [chan <! close!]]
            [cljs-http.client :as http]
            [clojure.string :as s]
            [goog.string :as gstring]
            [goog.string.format :as gformat])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; New utils go here

(defn navigate! [hash]
  (set! (.-hash js/location) hash))

(defn code->key
  [code]
  (-> code s/trim s/lower-case keyword))

(defn tocurrency
  "Format a number for currency display (2 digits. Parentheses when negative)"
  [v]
  (double v))

(defn tonumber
  ([v curr]
    (cond
     (nil? v) ""
     (js/isNaN v) ""
     v (gstring/format "%s%0.2f" curr v)
     :else ""))
  ([v]
   (tonumber v "")))

(defn toprice [p]
  (tonumber p "£"))

(defn addvat [v]
  (if (= v "Z") " +VAT" ""))

(defn has-role?
  [app role]
  (when (get-in app [:login :loggedin])
    (some #{role} (get-in app [:login :user :roles]))))

;;==============================================================================
;; TODO remove everything below here, eventually
;;==============================================================================
(def appstate
  "appstate has the following structure:
   {:user {:username ...
           :roles ... }
    :message \"any auth failure message\"
    :order {:item1code {item1data}
            :item2code {item2data}
            ...}
    :allorders {
      :orderdate date
      :items {
        :item1code {
          :itemdata {item1-general-data}
          :orders {
             :member1code {member1-order}
             :member2code {member2-order}
          }}}}}"

(reagent/atom nil))



(defn all-orders
  []
  (:allorders @appstate))

(defn get-allorders!
  [orderdate]
  (go
    (let [path "/orders/%s"
          resource (gstring/format path orderdate)
          {:keys [status body] :as response} (<! (http/get resource))]
      (prn "get-allorders! body" body)
      (swap! appstate assoc  :allorders body))))
