(ns ordinator.prod
  (:require [cljs.ordinator.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
