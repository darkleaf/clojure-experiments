(ns shunting-yard.core
  (:require [clojure.string :as str]))

(def token-types [{:pattern #"\d+", :type :number}
                  {:pattern #"\(", :type :open-paren}
                  {:pattern #"\)", :type :close-paren}
                  {:pattern #"\+", :type :operator, :priority 1, :fn +}
                  {:pattern #"\-", :type :operator, :priority 1, :fn -}
                  {:pattern #"\*", :type :operator, :priority 2, :fn *}
                  {:pattern #"\/", :type :operator, :priority 2, :fn /}])

(defmulti process-token (fn [token _ _] (:type token)))
(defmulti exec-token (fn [token _] (:type token)))

(defn- build-token [value]
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

(defn- dismantle-tokens
  "Разбирает токены на стек и очередь"
  ([tokens] (dismantle-tokens tokens '() clojure.lang.PersistentQueue/EMPTY))
  ([[token & rest] stack queue]
   (if token
     (let [[new-stack new-queue] (process-token token stack queue)]
       (recur rest new-stack new-queue))
     [stack queue])))

(defn- gathering-tokens [[stack queue]]
  "Собирает токены из стека и очереди"
  (if (not-empty stack)
    (recur [(pop stack) (conj queue (peek stack))])
    queue))

(defn shunting-yard [tokens]
  (-> tokens
      dismantle-tokens
      gathering-tokens))

(defn- exec
  ([tokens] (exec tokens '()))
  ([[token & rest] stack]
   (if token
     (let [new-stack (exec-token token stack)]
       (recur rest new-stack))
     (peek stack))))

(defn calc [exp]
  (-> exp
      tokenizer
      shunting-yard
      exec))

(defmethod process-token :number [token stack queue]
  [stack (conj queue token)])

(defmethod process-token :operator [op1 [op2 & rest :as stack] queue]
  (println op1 stack queue)
  (if (and
       (= :operator (:type op2))
       (<= (:priority op1) (:priority op2)))
    (recur op1 rest (conj queue op2))
    [(conj stack op1) queue]))

(defmethod process-token :open-paren [token stack queue]
  [(conj stack token) queue])

(defmethod process-token :close-paren [token [head & rest :as stack] queue]
  (if (not= :open-paren (:type head))
    (recur token (pop stack) (conj queue (peek stack)))
    [(pop stack) queue]))

(defmethod exec-token :number [token stack]
  (conj stack (-> token :value bigint)))

(defmethod exec-token :operator [token stack]
  (let [arg1 (-> stack pop peek)
        arg2 (-> stack peek)
        new-stack (-> stack pop pop)
        token-fn (:fn token)
        result (token-fn arg1 arg2)]
    (conj new-stack result)))
