(ns shunting-yard.core
  (:require [clojure.string :as str]))

(def token-types [{:pattern #"\d+", :type :number}
                  {:pattern #"\(", :type :open-bracket}
                  {:pattern #"\)", :type :close-bracket}
                  {:pattern #"\+", :type :operator, :priority 1, :fn +}
                  {:pattern #"\-", :type :operator, :priority 1, :fn -}
                  {:pattern #"\*", :type :operator, :priority 2, :fn *}
                  {:pattern #"\/", :type :operator, :priority 2, :fn /}])

(defn build-token [value]
  (as-> value x
    (fn [{:keys [pattern]}] (re-matches pattern x))
    (filter x token-types)
    (first x)
    (assoc x :value value)))

(defn tokenizer [expr]
  (let [pattern (->> token-types
                     (map :pattern)
                     (str/join "|")
                     (re-pattern))]
    (->> expr
      (re-seq pattern)
      (map build-token))))

(defmulti handle-token (fn [token _ _] (:type token)))

(defn polish
  ([tokens] (polish tokens '() clojure.lang.PersistentQueue/EMPTY))
  ([[token & rest] stack queue]
   (print token)

   (if token
     (do
       (let [[new-stack new-queue] (handle-token token stack queue)]
         (recur rest new-stack new-queue)))
     (do
       (loop [final-stack stack
              final-queue queue]
         (if (not-empty final-stack)
           (do
             ;;bracket
             (recur (pop final-stack) (conj final-queue (peek final-stack))))
           final-queue))))))

(defmethod handle-token :number [token stack queue]
  [stack (conj queue token)])

(defmethod handle-token :operator [token stack queue]
  [(conj stack token) queue])


(defmulti exec-token (fn [token _] (:type token)))

(defmethod exec-token :number [token stack]
  (conj stack (-> token :value bigint)))

(defmethod exec-token :operator [token stack]
  (let [arg1 (-> stack pop peek)
        arg2 (-> stack peek)
        fn (:fn token)
        result (fn arg1 arg2)]
    (conj stack result)))

(defn exec
  ([tokens] (exec tokens '()))
  ([[token & rest] stack]
   (if token
     (let [new-stack (exec-token token stack)]
       (recur rest new-stack))
     (peek stack))))

(defn calc [exp]
  (-> exp
      tokenizer
      polish
      exec))
