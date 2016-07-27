(ns cljs.ordinator.messages)

(defrecord NavigateToOrder [app])

(defrecord NavigateToAllOrders [app])

(defrecord Login [submessage])

(defrecord Order [submessage])

(defrecord AllOrders [submessage])
