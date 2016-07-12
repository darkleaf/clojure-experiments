(ns shunting-yard.core-test
  (:require [clojure.test :refer :all]
            [shunting-yard.core :refer :all]))

(deftest calc-test
  (are [x y] (= x (calc y))
    42 "40 + 2"))

(deftest tokeinzer-test
  (are [x y] (= x (->> y (tokenizer) (map :value)))
    ["40" "+" "2"] "40 + 2"))

(deftest polish-test
  (are [x y] (= x (->> y (tokenizer) (shunting-yard) (map :value)))
    ["40" "2" "+"] "40 + 2"))
