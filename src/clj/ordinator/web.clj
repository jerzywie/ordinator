(ns ordinator.web
  (:require [ordinator.catalogue :as cat]
            [compojure
             [core :refer [defroutes GET PUT]]
             [route :as route]]
            [environ.core :refer [env]]
            [metrics.ring
             [expose :refer [expose-metrics-as-json]]
             [instrument :refer [instrument]]]
            [ordinator.page-frame :refer [page-frame]]
            [prone.middleware :refer [wrap-exceptions]]
            [radix
             [error :refer [error-response wrap-error-handling]]
             [ignore-trailing-slash :refer [wrap-ignore-trailing-slash]]
             [reload :refer [wrap-reload]]
             [setup :as setup]]
            [ring.middleware
             [format-params :refer [wrap-json-kw-params]]
             [json :refer [wrap-json-response]]
             [params :refer [wrap-params]]]))

(def version
  (setup/version "ordinator"))

(def dev-mode?
  (boolean (env :dev-mode false)))

(defn healthcheck
  []
  (let [body {:name "ordinator"
              :version version
              :success true
              :dependencies []}]
    {:headers {"content-type" "application/json"}
     :status (if (:success body) 200 500)
     :body body}))

(defn get-catalogue
  []
  {:status 200
   :body (cat/get-catalogue)})

(defn update-catalogue
  []
  {:status 200
   :body (cat/update-catalogue)})

(defroutes routes

  (GET "/healthcheck"
       [] (healthcheck))

  (GET "/ping"
       [] "pong")

  (GET "/"
       [] (page-frame dev-mode?))

  (GET "/catalogue"
       [] (get-catalogue))

  (PUT "/catalogue"
       [] (update-catalogue))

  (route/not-found (error-response "Resource not found" 404)))

(def app
  (-> routes
      (cond-> dev-mode? wrap-exceptions)
      (wrap-reload)
      (instrument)
      (wrap-error-handling)
      (wrap-ignore-trailing-slash)
      (wrap-json-response)
      (wrap-json-kw-params)
      (wrap-params)
      (expose-metrics-as-json)))
