(ns cljs.ordinator.order.messages)

(defrecord GetOrder [username])

(defrecord OrderResult [body])

(defrecord DeleteOrderLine [code])
