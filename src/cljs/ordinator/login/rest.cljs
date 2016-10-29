(ns cljs.ordinator.login.rest
  (:require [cljs-http.client :as http]
            [petrol.core :as petrol]
            [cljs.ordinator.login.messages :as m]))

(defn do-login
  [username password]
  (let [response (http/post
                  "/v1/login"
                  {:json-params
                   {:username username :password password}})]
    (petrol/wrap m/map->LoginResult response)))

(defn do-logout []
  (let [response (http/delete "/v1/login" )]
    (petrol/wrap m/map->LogoutResult response)))
