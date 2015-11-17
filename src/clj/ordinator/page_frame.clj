(ns ordinator.page-frame
  (:require [hiccup
             [core :refer [html]]
             [page :refer [html5 include-css include-js]]]))

(defn page-frame
  [dev-mode?]
  (html5
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
      [:p "Loading..."]]
     (include-js "https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js")
     (include-js "js/bootstrap.min.js")
     (include-js "js/app.js")]]))
