(defproject ordinator "1.0.0"
  :description "Ordinator service"

  :dependencies [[org.clojure/core.cache "0.6.4"]
                 [ch.qos.logback/logback-classic "1.1.3"]
                 [cheshire "5.4.0"]
                 [clj-http "1.1.2"]
                 [clj-time "0.9.0"]
                 [compojure "1.3.4"]
                 [environ "1.0.0"]
                 [com.codahale.metrics/metrics-logback "3.0.2"]
                 [mixradio/graphite-filter "1.0.0"]
                 [mixradio/instrumented-ring-jetty-adapter "1.0.4"]
                 [mixradio/radix "1.0.10"]
                 [net.logstash.logback/logstash-logback-encoder "4.3"]
                 [org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ring/ring-json "0.3.1"]
                 [ring-middleware-format "0.5.0"
                  :exclusions [org.clojure/clojure
                               commons-logging
                               log4j
                               org.clojure/core.cache]]
                 [dk.ative/docjure "1.6.0"]
                 [cljsjs/react "0.13.3-1"]
                 [hiccup "1.0.5"]
                 [com.cemerick/friend "0.2.1"]
                 [org.clojure/clojurescript "1.8.40" :scope "provided"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"
                  :exclusions [org.clojure/clojure
                               commons-logging
                               log4j
                               org.clojure/core.cache]]
                 [prone "0.8.2"]
                 [petrol "0.1.3"]
                 [reagent "0.5.1"]
                 [reagent-utils "0.1.7"]
                 [cljs-http "0.1.37"
                  :exclusions [org.clojure/clojure
                               commons-logging
                               log4j
                               org.clojure/core.cache]]
                 [com.cemerick/friend "0.2.1"]
                 [org.marianoguerra/friend-json-workflow "0.2.1"]
                 [com.taoensso/faraday "1.7.1"]
                 [bidi "1.24.0"]
                 [kibu/pushy "0.3.6"]]

  :exclusions [commons-logging
               log4j
               org.clojure/clojure
               cljsjs/react-with-addons]

  :plugins [[lein-environ "1.0.0"]
            [lein-release "1.0.5"]
            [lein-ring "0.8.12"]
            [lein-asset-minifier "0.2.3"]]

  :source-paths ["src/clj" "src/cljs"]

  :env {:auto-reload "true"
        :environment-name "poke"
        :graphite-enabled "false"
        :graphite-host ""
        :graphite-port "2003"
        :graphite-post-interval-seconds "60"
        :logging-consolethreshold "info"
        :logging-filethreshold "info"
        :logging-level "info"
        :logging-path "/tmp"
        :logging-stashthreshold "off"
        :production "false"
        :requestlog-enabled "false"
        :requestlog-retainhours "24"
        :restdriver-port "8081"
        :service-name "ordinator"
        :service-port "8080"
        :service-url "http://localhost:%s"
        :shutdown-timeout-millis "5000"
        :start-timeout-seconds "120"
        :threads "254"}

  :lein-release {:deploy-via :shell
                 :shell ["lein" "do" "clean," "uberjar," "pom," "rpm"]}

  :ring {:handler ordinator.web/app
         :main ordinator.setup
         :port ~(Integer/valueOf (get (System/getenv) "SERVICE_PORT" "8080"))
         :init ordinator.setup/setup
         :browser-uri "/healthcheck"
         :nrepl {:start? true}}

  :uberjar-name "ordinator.jar"

  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                             :figwheel {:on-jsload "cljs.ordinator.core/reload-hook"}
                             :compiler {:output-to "resources/public/js/app.js"
                                        :output-dir "resources/public/js/out"
                                        :asset-path "js/out"
                                        :optimizations :none
                                        :pretty-print  true}}}}

  :clean-targets ^{:protect false} [:target-path
                                    [:cljsbuild :builds :app :compiler :output-dir]
                                    [:cljsbuild :builds :app :compiler :output-to]]

  :minify-assets {:assets {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :profiles {:dev {:dependencies [[com.github.rest-driver/rest-client-driver "1.1.42"
                                   :exclusions [org.slf4j/slf4j-nop
                                                javax.servlet/servlet-api
                                                org.eclipse.jetty.orbit/javax.servlet]]
                                  [junit "4.12"]
                                  [midje "1.6.3"]
                                  [rest-cljer "0.1.20"]
                                  [lein-figwheel "0.5.2"
                                   :exclusions [org.clojure/clojure
                                                commons-logging
                                                log4j
                                                org.clojure/core.cache
                                                org.clojure/core.memoize
                                                ring/ring-core
                                                org.ow2.asm/asm-all
                                                org.clojure/data.priority-map
                                                org.clojure/tools.reader
                                                org.clojure/clojurescript
                                                org.clojure/core.async
                                                org.clojure/tools.analyzer.jvm]]
                                  [org.clojure/tools.nrepl "0.2.12"]
                                  [com.cemerick/piggieback "0.2.1"]]

                   :plugins [[lein-kibit "0.0.8"]
                             [lein-midje "3.1.3"]
                             [lein-rpm "0.0.5"]
                             [lein-figwheel "0.5.2"
                              :exclusions [org.clojure/clojure
                                           commons-logging
                                           log4j
                                           org.clojure/core.cache
                                           org.clojure/core.memoize
                                           ring/ring-core
                                           org.ow2.asm/asm-all
                                           org.clojure/data.priority-map
                                           org.clojure/tools.reader
                                           org.clojure/clojurescript
                                           org.clojure/core.async
                                           org.clojure/tools.analyzer.jvm]]
                             [lein-cljsbuild "1.1.1"]]

                   :env {:dev-mode true}

                   :repl-options {:init-ns ordinator.repl}

                   :source-paths ["env/dev/clj"]

                   :figwheel {:http-server-root "public"
                              :server-port 8088
                              :nrepl-port 7002
                              :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"]
                              :css-dirs ["resources/public/css"]
                              ;:ring-handler ordinator.web/app
                              }

                   :cljsbuild {:builds {:app {:source-paths ["env/dev/cljs"]
                                              :compiler {:main "ordinator.dev"
                                                         :source-map true}}}}}

             :uberjar {:hooks [leiningen.cljsbuild minify-assets.plugin/hooks]
                       :aot :all
                       :omit-source true
                       :cljsbuild {:jar true
                                   :builds {:app
                                            {:source-paths ["env/prod/cljs"]
                                             :compiler
                                             {:optimizations :advanced
                                              :pretty-print false}}}}}}

  :rpm {:name "ordinator"
        :summary "RPM for Ordinator service"
        :copyright "MixRadio 2015"
        :preinstall {:scriptFile "scripts/rpm/preinstall.sh"}
        :postinstall {:scriptFile "scripts/rpm/postinstall.sh"}
        :preremove {:scriptFile "scripts/rpm/preremove.sh"}
        :postremove {:scriptFile "scripts/rpm/postremove.sh"}
        :requires ["jdk >= 2000:1.7.0_55-fcs"]
        :mappings [{:directory "/usr/local/ordinator"
                    :filemode "444"
                    :username "ordinator"
                    :groupname "ordinator"
                    :sources {:source [{:location "target/ordinator.jar"}]}}
                   {:directory "/usr/local/ordinator/bin"
                    :filemode "744"
                    :username "ordinator"
                    :groupname "ordinator"
                    :sources {:source [{:location "scripts/bin"}]}}
                   {:directory "/etc/rc.d/init.d"
                    :filemode "755"
                    :sources {:source [{:location "scripts/service/ordinator"
                                        :destination "ordinator"}]}}]}

  :aot [ordinator.setup]

  :main ordinator.setup)
