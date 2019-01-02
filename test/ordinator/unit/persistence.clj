(ns ordinator.unit.persistence
  (:require [ordinator.dynamo :refer :all]
            [ordinator.test-common :as test]
            [midje.sweet :refer :all]))

(fact-group
 :persistence

 (fact "can retrieve a previously saved record"
       (let [user-data (assoc test/user-record :userid (test/uuid))
             {:keys [userid username name email roles] :as result} user-data]
         (save-user userid username name email roles)
         (get-user-by-userid userid) => user-data))

 (fact "save-user throws exception if the userid is not unique"
       (let [user-data (assoc test/user-record :userid (test/uuid))
             {:keys [userid username name email roles] :as result} user-data]
         (prn "user-data " user-data)
         (prn "Destructured. userid: " userid " username: " username " name: " name " email: " email)
         (prn "Result " result)
         (save-user userid username name email roles) => result
         (save-user userid username name email roles) => (throws Exception)))

 (fact "1 = 1"
       1 => 1))
