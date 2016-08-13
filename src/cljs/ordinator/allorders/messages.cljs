(ns cljs.ordinator.allorders.messages)

(defrecord GetAllOrders [orderdate])

(defrecord GetAllOrdersResult [body])

(defrecord EditItem [code])

(defrecord RevertEditing [])

(defrecord ChangeQuantity [member quantity])

(defrecord KeyEvent [c])

(defrecord DeleteItem [code])

(defrecord DeleteItemReally [code])

(defrecord SaveItem [code])

(defrecord SaveItemResult [body])

(defrecord DoNothing [code]) ; this is temporary
