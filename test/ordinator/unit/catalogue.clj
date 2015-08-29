(ns ordinator.unit.catalogue
  (:require [ordinator.catalogue :refer :all]
            [midje.sweet :refer :all]))

(fact-group
 :unit :get-units

 (fact "get-units returns correct analysis of sub-divisible packsize"
       (get-units "3 x 10ml")  => {:unit "1 x 10ml" :unitsperpack 3 :splits? false}
       (get-units " 6*(2x180g)")  => {:unit "1*(2x180g)" :unitsperpack 6 :splits? true}
       (get-units "6 * 283g")  => {:unit "1 * 283g" :unitsperpack 6 :splits? true}
       (get-units "12*135g")  => {:unit "1*135g" :unitsperpack 12 :splits? true}
       (get-units "10*(2x175g")  => {:unit "1*(2x175g" :unitsperpack 10 :splits? true}

       (get-units "6x4x125g")  => {:unit "1x4x125g" :unitsperpack 6 :splits? false})

 (fact "get-units returns correct analysis of bulk packsize"
       (get-units "2.5 ltr")  => {:unit "2.5 ltr" :unitsperpack 1 :splits? false}
       (get-units "2.5kg")  => {:unit "2.5kg" :unitsperpack 1 :splits? false})

 (fact "get-units returns correct analysis for various odd cases"
       (get-units "1 Off")  => {:unit "1 Off" :unitsperpack 1 :splits? false}
       (get-units "unrecognised")  => {:unit "unrecognised" :unitsperpack 1 :splits? false}))

(fact-group
 :unit :update-cat

 (fact "update-catalogue returns correct data structure"
       (update-catalogue) => {:acode {:code "acode"
                                      :packsize "3*blah"
                                      :unitsperpack 3
                                      :unit "1*blah"
                                      :splits? true}}
       (provided
        (read-cat-file) => [{:header "header"} {:code "acode" :packsize "3*blah"}]
        (get-units "3*blah") => {:unitsperpack 3 :unit "1*blah" :splits? true})))
