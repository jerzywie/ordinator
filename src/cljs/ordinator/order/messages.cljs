(ns cljs.ordinator.order.messages)

(defrecord GetOrder [username])

(defrecord OrderResult [body])

(defrecord DeleteOrderLine [code])

(defrecord GetCatalogue [])

(defrecord GetCatalogueResult [body])

(defrecord ChangeCode [codestr])

(defrecord ChangeQuantity [quantity])

(defrecord AddItem [])

(defrecord SaveOrder [])

(defrecord SaveOrderResult [body])
