(ns ordinator.acceptance.user
  (:require [ordinator
             [user :refer :all]
             [test-common :as test]
             [dynamo :refer [client-opts ensure-tables]]
             [role :as role]]
            [midje.sweet :refer :all]
            [taoensso.faraday :as far]))

(defn delete-table
  []
  (far/delete-table client-opts :users))

(defn create-some-users
  [n]
  (let [base-user-record {:username "user-name-"
                          :name "first-and-lastname-"
                          :password "xyzzy-"
                          :email "not-a-real-email-"}
        role {:roles #{::role/user}}
        create-fn (fn [index] (let [urec (apply merge
                                               (map (fn [[k v]] {k (str v index)})
                                                    base-user-record))]
                               (create-user (merge urec role))))]
    (->> (doall (map create-fn (take n (drop 1 (range)))))
         (map :userid))))

(fact-group
 :acceptance

 (with-state-changes [(before :contents (do (ensure-tables)))
                      (after :contents (do (delete-table)
                                        (ensure-tables)))]

   (fact "can create a user with all required attributes"
         (create-user {:username "uname"
                       :name "firstname lastname"
                       :password "xyzzy"
                       :email "another@example.com"
                       :roles #{::role/user}}) => (just {:userid anything
                                                         :username "uname"
                                                         :name "firstname lastname"
                                                         :password anything
                                                         :email "another@example.com"
                                                         :active? true
                                                         :roles #{::role/user}}))

   (fact "failure to supply all required attributes throws exception"
         (create-user {:name "firstname lastname"
                       :password "xyzzy"
                       :email "another@example.com"
                       :roles #{::role/user}}) => (throws Exception)

         (create-user {:username "uname"
                       :password "xyzzy"
                       :email "another@example.com"
                       :roles #{::role/user}}) => (throws Exception)

         (create-user {:username "uname"
                       :name "firstname lastname"
                       :email "another@example.com"
                       :roles #{::role/user}}) => (throws Exception)

         (create-user {:username "uname"
                       :name "firstname lastname"
                       :password "xyzzy"
                       :roles #{::role/user}}) => (throws Exception)

         (create-user {:username "uname"
                       :name "firstname lastname"
                       :password "xyzzy"
                       :email "another@example.com"}) => (throws Exception))

   (fact "can retrieve user by username"
         (let [user1 (create-user {:username "uname1"
                                   :name "firstname lastname"
                                   :password "xyzzy"
                                   :email "another1@example.com"
                                   :roles #{::role/user}})
               user2 (create-user {:username "uname2"
                                   :name "firstname2 lastname2"
                                   :password "xyzzy2"
                                   :email "another2@example.com"
                                   :roles #{::role/user}})]
           (:username (find-user-by-username "uname1")) =>  "uname1"))

   (fact "can manipulate existing user records"
         (let [user1 (create-user {:username "uname11"
                                   :name "firstname1 lastname1"
                                   :password "xyzzy"
                                   :email "another1@example.com"
                                   :roles #{::role/user}})
               user2 (create-user {:username "uname22"
                                   :name "firstname2 lastname2"
                                   :password "abracadabra"
                                   :email "another@example.com"
                                   :roles #{::role/user}})
               userid1 (:userid user1)
               original-password-user1 (:password user1)
               userid2 (:userid user2)]

           (fact "nil userid throws"
                 (update-user nil {:username "ya-username"}) => (throws Exception))

           (fact "update with other users username throws"
                 (update-user userid1 {:username "uname2"}) => (throws Exception))

           (fact "user update succeeded"
                 (let [updated-details {:email "newemail@example.com" :new-attr "nu-attr"}]
                   (update-user userid1 updated-details) => (contains updated-details)))

           (let [update-result (update-user userid1 {:password "mellon"})]
             (fact "password update succeeds"
                   update-result =not=> (contains {:password original-password-user1}))

             (fact "updated password is encrypted"
                   (:password update-result) =not=> "mellon"))

           (fact "can disable a user"
                 (disable-user userid1) => (contains {:active? false}))))
   ))

(fact-group
 :acceptance

 (with-state-changes [(before :contents (do (ensure-tables)))
                      (after :contents (do (delete-table)
                                          (ensure-tables)))]

   (fact "get-active-users only returns active users"
         (let [num-users 5
               num-to-disable 2
               userids (create-some-users num-users)]

           (fact "all users active on creation"
                 (count (get-active-users)) => num-users)

           (fact "disabled users are takein into account"
                 (doall (map disable-user (take num-to-disable userids)))
                 (count (get-active-users)) => (- num-users num-to-disable))))
   ))
