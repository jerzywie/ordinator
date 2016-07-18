(ns cljs.ordinator.messages)

(defrecord NavigateToOrder [app])

(defrecord Login [submessage])

(defrecord Order [submessage])
