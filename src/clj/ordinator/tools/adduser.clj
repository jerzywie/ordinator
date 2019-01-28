(ns ordinator.tools.adduser
  (:require [ordinator
             [user :refer [create-user]]
             [dynamo :as db]]
            [clojure.set :as set]))

(def expected-args 5)

(defn- print-usage []
  (println "Usage: username name password email role(s)")
  (println "       roles are: user|coordinator|admin"))

(defn make-roles-set
  [role-list]
  (->> role-list
       (map (comp keyword (partial str "ordinator.role/")))
       set))

(defn -main
  [& args]
  (if (< (count args) expected-args)
    (print-usage)
    (do (prn "args " args)
        (let [[username name password email role] args
              more-roles (drop expected-args args)
              roles-list (make-roles-set (cons role more-roles))
              user-record {:username username
                           :name name
                           :password password
                           :email email
                           :roles roles-list}]
          (prn "Adding user-record: " user-record)
          (create-user user-record)))))
