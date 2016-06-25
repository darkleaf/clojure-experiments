(ns football.core-test
  (:require [clojure.test :refer :all]
            [football.core :refer :all]))

(deftest football
  (testing "score"
    (is (= (score "2:1" "2:1") 2))
    (is (= (score "1:1" "2:2") 1))
    (is (= (score "2:1" "2:0") 1))
    (is (= (score "2:1" "1:2") 0))))

