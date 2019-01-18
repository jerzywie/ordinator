(ns ordinator.role)

#?(:clj (derive ::admin ::user))

#?(:clj (derive ::admin ::coordinator))

#?(:clj (derive ::coordinator ::user))

#?(:cljs (def roles {::user #{} ::coordinator #{::user} ::admin #{::user ::coordinator} }))

#?(:cljs (defn has-role?
           [user-roles role]
           (let [role (keyword role)
                 user-roles (map keyword user-roles)
                 all-roles (->>
                            (map (partial get roles) user-roles)
                            (reduce clojure.set/union user-roles))]
             (some #{role} all-roles))))
