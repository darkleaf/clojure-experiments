(ns shunting-yard.core-test
  (:require [clojure.test :refer :all]
            [shunting-yard.core :refer :all]))

(deftest calc-test
  (are [x y] (= x (calc y))
    42 "40 + 2"
    8 "2 + 2 * 3"
    4 "2 * (3 - 1)"
    3 "2 / 2 * 3"
    6 "(2 * (2 + (4 / 2) - 1))"))

(deftest tokeinzer-test
  (are [x y] (= x (->> y (tokenizer) (map :value)))
    ["40" "+" "2"] "40 + 2"))

(deftest polish-test
  (are [x y] (= x (->> y (tokenizer) (shunting-yard) (map :value)))
    ["40" "2" "+"] "40 + 2"))
