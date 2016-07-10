(ns ^:figwheel-no-load ordinator.dev
  (:require [cljs.ordinator.core :as core]
            [figwheel.client :as figwheel :include-macros true]))

(enable-console-print!)

(figwheel/watch-and-reload
  :websocket-url "ws://localhost:8080/figwheel-ws")

(core/init!)
