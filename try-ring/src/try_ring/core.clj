(ns try-ring.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [mount.core :refer [defstate] :as mount])
  (:gen-class))

(def handler)

(defstate http-server
  :start (run-jetty handler {:port 3001 :join? false})
  :stop (.stop http-server))

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello World"})

(defn -main
  [& args]
  (mount/start))
