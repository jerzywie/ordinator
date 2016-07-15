(ns cljs.ordinator.login.rest
  (:require [cljs-http.client :as http]
            [petrol.core :as petrol]
            [cljs.ordinator.login.messages :as m]))

(defn do-login
  [username password]
  (let [response (http/post
                  "/login"
                  {:json-params
                   {:username username :password password}})]
    (petrol/wrap m/map->LoginResult response)))
