(ns ordinator.acceptance.persistence
  (:require [ordinator
             [dynamo :refer :all]
             [test-common :as test]
             [role :as role]]
            [midje.sweet :refer :all]
            [taoensso.faraday :as far]))

(defn- make-unique-user
  ([username]
   (assoc test/user-record :userid (test/uuid) :username username :active? true :password "pwd"))
  ([]
   (make-unique-user (test/uuid))))

(defn delete-table
  []
  (far/delete-table client-opts :users))

(fact-group
 :acceptance

 (with-state-changes [(before :facts (do (ensure-tables)))
                      (after :facts (do (delete-table)
                                        (ensure-tables)))]

   (fact "can retrieve a previously created user-record by id"
         (let [user-data (make-unique-user)
               {:keys [userid username name email roles] :as result} user-data
               cur (create-user user-data)]
           (get-user-by-userid userid) => user-data))

   (fact "handling of non-existent records"
         (let [none-such (test/uuid)]
           (fact "non-existent userid returns nil"
                 (get-user-by-userid none-such) => nil)
           (fact "non-existent username returns nil"
                 (get-user-by-username none-such) => nil)))

   (fact "can retrieve a previously created user-record by username"
         (let [create-user-data (make-unique-user)
               {:keys [userid username name email roles]} create-user-data
               name-and-id [username userid]
               expect {:userid userid :username username :password ..anything.. :roles ..anything..}]
           (create-user create-user-data)
           (let [user-data-result (get-user-by-username username)]
             (keys expect)  => (contains (keys user-data-result) :in-any-order)
             (let [{:keys [username userid]} user-data-result]
               [username userid] => name-and-id))))

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

   (fact "get-active-users gets only active users"
         (let [user1 (make-unique-user "fred")
               user2 (assoc (make-unique-user "joe") :active? false)
               user3 (make-unique-user "kate")
               user1-result (create-user user1)
               user2-result (create-user user2)
               user3-result (create-user user3)]
           (set (map :username (get-active-users))) => (set '("fred" "kate"))))

   (defn- keyword-roles?
     [{:keys [roles]}]
     (prn "keyword-roles? " roles)
     (->> roles
          (map keyword?)
          (some false?)))

   (defn report
     [msg fn]
     (let [result (fn)]
       (prn "report " msg ": " result)
       result))

   (fact "roles are returned as keywords in all cases"
         (let [user-data (assoc (make-unique-user) :roles #{::role/user ::role/admin})
               create-user-response (create-user user-data)
               {:keys [username userid]} create-user-response
               user-by-id-response (get-user-by-userid userid)
               user-by-name-response (get-user-by-username username)
               update-user-response (update-user {:userid userid :username "new-name"})
               update-user-roles-response (update-user {:userid userid :roles #{::role/coordinator}})]

           (fact "create-user"
                 (keyword-roles? create-user-response) => nil)

           (fact "get-user-by-id"
                 (keyword-roles? user-by-id-response) => nil)

           (fact "get-user-by-username"
                 (keyword-roles? user-by-name-response) => nil)

           (fact "update-user (not updating :roles"
                 (keyword-roles? update-user-response) => nil)

           (fact "update-user (when :roles updated"
                 (keyword-roles? update-user-roles-response) => nil)))
   ))
