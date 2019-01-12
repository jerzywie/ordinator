(ns ordinator.unit.persistence
  (:require [ordinator.dynamo :refer :all]
            [ordinator.test-common :as test]
            [midje.sweet :refer :all]))

(defn- make-unique-user
  []
  (assoc test/user-record :userid (test/uuid) :username (test/uuid)))

(fact-group
 :persistence

 (fact "can retrieve a previously created user-record by id"
       (let [user-data (make-unique-user)
             {:keys [userid username name email roles] :as result} user-data]
         (create-user user-data)
         (get-user-by-userid userid) => user-data))


 (fact "handling of non-existent records"
       (let [none-such (test/uuid)]
         (fact "non-existent userid returns nil"
               (get-user-by-userid none-such) => nil)
         (fact "non-existent username returns nil"
               (get-user-by-username none-such) => nil)))

 (fact "can retrieve a previously created user-record by username"
       (let [user-data (make-unique-user)
             {:keys [userid username name email roles]} user-data
             result {:userid userid :username username}]
         (create-user user-data)
         (get-user-by-username username) => result))

 (fact "create-user throws exception if the userid is not unique"
       (let [user-data (make-unique-user)
             {:keys [userid username name email roles] :as result} user-data]
         (create-user user-data) => result
         (create-user user-data) => (throws Exception)))

 (fact "create-user throws exception if the username is not unique"
       (let [username (str "user-" (test/uuid))
             userid1 (test/uuid)
             userid2 (test/uuid)
             user1 (assoc test/user-record :userid userid1 :username username)
             user2 (assoc test/user-record :userid userid2 :username username)]
         (create-user user1) => (assoc test/user-record :userid userid1 :username username)
         (create-user user2) => (throws Exception)))

 (fact "update-user allows update of existing user record"
       (let [original-details (create-user (make-unique-user))
             userid (:userid original-details)
             update-details {:email "wasfred@example.com" :userid userid :username (test/uuid)}]
         (update-user update-details) => (merge original-details update-details)))

 (fact "user must already exist in order to be updated"
       (let [no-such-user (make-unique-user)
             userid (:userid no-such-user)
             update-details {:email "wasfred@example.com" :userid userid}]
         (update-user update-details) => (throws Exception)))

 (fact "updated username must be unique"
       (let [user1 (make-unique-user)
             user2 (make-unique-user)
             user1-result (create-user user1)
             user2-result (create-user user2)
             user2-update {:userid (:userid user2) :username (:username user1)}]
         (update-user user2-update) => (throws Exception)))
)
