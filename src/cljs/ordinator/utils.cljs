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

(defn cost-to-user
  [user-quantity units-per-pack pack-price]
  (* (/ user-quantity units-per-pack) pack-price))
