(ns try-ring.dev
  (:require [mount.core :as mount]
            [try-ring.core]))

(defn start [] (mount/start #'try-ring.core/http-server))
(defn stop [] (mount/stop))
