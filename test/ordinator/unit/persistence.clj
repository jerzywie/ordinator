(ns ordinator.unit.persistence
  (:require [ordinator.dynamo :refer :all]
            [ordinator.test-common :as test]
            [midje.sweet :refer :all]))

(defn make-unique-user
  []
  (assoc test/user-record :userid (test/uuid) :username (test/uuid)))

(fact-group
 :persistence

 (fact "can retrieve a previously saved user-record by id"
       (let [user-data (make-unique-user)
             {:keys [userid username name email roles] :as result} user-data]
         (save-user userid username name email roles)
         (get-user-by-userid userid) => user-data))


 (fact "handling of non-existent records"
       (let [none-such (test/uuid)]
         (fact "non-existent userid returns nil"
               (get-user-by-userid none-such) => nil)
         (fact "non-existent username returns nil"
               (get-user-by-username none-such) => nil)))

 (fact "can retrieve a previously saved user-record by username"
       (let [user-data (make-unique-user)
             {:keys [userid username name email roles]} user-data
             result {:userid userid :username username}]
         (save-user userid username name email roles)
         (get-user-by-username username) => result))

 (fact "save-user throws exception if the userid is not unique"
       (let [user-data (make-unique-user)
             {:keys [userid username name email roles] :as result} user-data]
         (prn "user-data " user-data)
         (prn "Destructured. userid: " userid " username: " username " name: " name " email: " email)
         (prn "Result " result)
         (save-user userid username name email roles) => result
         (save-user userid username name email roles) => (throws Exception)))

 (fact "save-user throws exception if the username is not unique"
       (let [username (str "user-" (test/uuid))
             userid1 (test/uuid)
             userid2 (test/uuid)
             {:keys [name email roles]} test/user-record]
         (save-user userid1 username name email roles) => (assoc test/user-record :userid userid1 :username username)
         (save-user userid2 username name email roles) => (throws Exception)))

)
