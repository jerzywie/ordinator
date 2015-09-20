(ns ordinator.web
  (:require [ordinator
             [catalogue :as cat]
             [auth :refer [wrap-json-authenticate login-form]]
             [page-frame :refer [page-frame]]
             [dynamo :as db]]
            [compojure
             [core :refer [defroutes GET PUT POST DELETE]]
             [route :as route]]
            [environ.core :refer [env]]
            [metrics.ring
             [expose :refer [expose-metrics-as-json]]
             [instrument :refer [instrument]]]
            [prone.middleware :refer [wrap-exceptions]]
            [radix
             [error :refer [error-response wrap-error-handling]]
             [ignore-trailing-slash :refer [wrap-ignore-trailing-slash]]
             [reload :refer [wrap-reload]]
             [setup :as setup]]
            [ring.middleware
             [format-params :refer [wrap-json-kw-params]]
             [json :refer [wrap-json-response]]
             [params :refer [wrap-params]]
             [session :refer [wrap-session]]
             [keyword-params :refer [wrap-keyword-params]]
             [nested-params :refer [wrap-nested-params]]]
            [cemerick.friend :as friend]
            [marianoguerra.friend-json-workflow :as json-auth]
            [ring.util.response :refer [response]]))

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

(defn get-user-order
  [user orderdate]
  (let [order (db/get-user-order user orderdate)]
    {:status 200
     :body order}))

(defn save-user-order
  [{:keys [route-params body-params]}]
  (db/save-user-order (merge route-params body-params)))

(defroutes routes

  (GET "/healthcheck"
       [] (healthcheck))

  (GET "/ping"
       [] "pong")

  (GET "/"
       [] (page-frame dev-mode?))

  (GET "/login"
       request (json-auth/handle-session request))

  (POST "/login"
       request (json-auth/handle-session request))

  (DELETE "/login"
          request (json-auth/handle-session request))

  (GET "/order/:user/:orderdate"
       [user orderdate]
       (get-user-order user orderdate))

  (PUT "/order/:user/:orderdate" req
       (save-user-order req))

  (GET "/catalogue"
       [] (get-catalogue))

  (PUT "/catalogue"
       [] (update-catalogue))

  (route/not-found (error-response "Resource not found!" 404)))

(defn mywrapper [{:keys [session] :as handler}]
  (fn [request]
    (let [response (handler request)]
      (prn "req: " request "resp: " response)
      response)))

(def app
  (-> routes
      (mywrapper)
      (wrap-json-authenticate)
      (wrap-session)
      (cond-> dev-mode? wrap-exceptions)
      (wrap-reload)
      (instrument)
      (wrap-error-handling)
      (wrap-ignore-trailing-slash)
      (wrap-json-response)
      (wrap-json-kw-params)
      (wrap-nested-params)
      (wrap-params)
      (expose-metrics-as-json)))
