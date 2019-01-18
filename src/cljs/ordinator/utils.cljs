(ns ordinator.utils
  (:require [ordinator.role :as role]
            [reagent.core :as reagent]
            [cljs.core.async :refer [chan <! close!]]
            [cljs-http.client :as http]
            [clojure.string :as s]
            [goog.string :as gstring]
            [goog.string.format :as gformat])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def vat-rates {:z 0 :f 5 :v 20})

(defn navigate! [hash]
  (set! (.-hash js/location) hash))

(defn code->key
  [codestr]
  (-> codestr s/trim s/lower-case keyword))

(defn key->code
  [codekey]
  (-> codekey name s/upper-case))

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

(defn tofloat
  [v]
  (cond
    (s/blank? v) 0
    (js/isNaN v) 0
    :else (js/parseFloat v)))

(defn vat-notice
  ([v prefix]
   (let [vat (cond
               (= v "V") "VAT"
               (= v "F") "Vf"
               :else nil)]
     (if vat
       (str prefix vat)
       "")))
  ([v]
   (vat-notice v nil)))

(defn add-vat-amount
  [price rate]
  (let [vatkey (-> rate s/lower-case keyword)
        vatrate (vatkey vat-rates)
        vat-amount (* price (/ vatrate 100))]
    (+ price vat-amount)))

(defn logged-in?
  [app]
  (get-in app [:login :loggedin]))

(defn has-role?
  [app role]
  (when (logged-in? app)
    (role/has-role? (get-in app [:login :user :roles]) role)))

(defn cost-to-user
  [user-quantity units-per-pack pack-price vat]
  (add-vat-amount (* (/ user-quantity units-per-pack) pack-price) vat))

(defn disable-it
  "Used to disable controls based on one or other of two fields being set to a value"
  [v-one v-two v-three]
  (or v-one (and v-two (not= v-two v-three))))

(defn valid-quantity?
  "Checks if the quantity is valid wrt unitsperpack
   For splittable quantites, allows any fraction which
   is an integer when multiplied by 100"
  [quantity unitsperpack splits?]
  (let [r (rem quantity unitsperpack)]
    (if-not splits?
      (= r 0)
      (integer? (* (/ r unitsperpack) 100)))))
