(ns ordinator.page-frame
  (:require [hiccup
             [core :refer [html]]
             [page :refer [include-css include-js]]]))

(defn page-frame
  [dev-mode?]
  (html
   [:html
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     [:title "Albany Ordinator"]
     (include-css (if dev-mode? "css/bootstrap.css" "css/bootstrap.min.css"))
     (include-css (if dev-mode? "css/site.css" "css/site.min.css"))]
    [:body
     [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]]
     (include-js "https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js")
     (include-js "js/bootstrap.min.js")
     (include-js "js/app.js")]]))
