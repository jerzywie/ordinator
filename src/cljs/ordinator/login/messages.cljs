(ns cljs.ordinator.login.messages)

(defrecord ChangeUsername [username])

(defrecord ChangePassword [password])

(defrecord DoLogin [])

(defrecord LoginResult [body])
