(ns football.core
  (:require [clojure.string :as str]))

(defn- parse-score [score]
  (as-> score v
    (str/split v #":")
    (map bigint v)))

(defn- winner [score]
  (let [[first second] (parse-score score)]
    (cond
      (> first second) :first
      (> second first) :second
      :else :both)))

(defn score [real user]
  (cond
    (= real user) 2
    (= (winner real) (winner user)) 1
    :else 0))


