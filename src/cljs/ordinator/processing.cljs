(ns cljs.ordinator.processing
  (:require [petrol.core :refer [Message
                                 process-message
                                 process-submessage
                                 EventSource
                                 watch-channels
                                 watch-subchannels]]
            [cljs.ordinator.messages :as m]
            [cljs.ordinator.login.processing]))

(extend-protocol Message
  m/Login
  (process-message [{:keys [submessage]} app]
    (prn "process-message submessage" submessage)
    (process-submessage submessage app [:login])))

(extend-protocol EventSource
  m/Login
  (watch-channels [{:keys [submessage]} app]
    (watch-subchannels submessage app [:login] m/->Login)))
