(ns cljs.ordinator.allorders.messages)

(defrecord GetAllOrders [orderdate])

(defrecord GetAllOrdersResult [body])

(defrecord EditItem [code])

(defrecord StopEditing [])

(defrecord ChangeQuantity [member quantity])

(defrecord KeyEvent [c])
