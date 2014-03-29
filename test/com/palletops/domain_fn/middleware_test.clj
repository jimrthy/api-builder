(ns com.palletops.domain-fn.middleware-test
  (:require
   [clojure.test :refer :all]
   [com.palletops.domain-fn :as dfn]
   [com.palletops.domain-fn.middleware :refer :all]
   [schema.core :as schema]))

;;; # Test add-meta
(dfn/def-defn defn-add-meta
  [(add-meta {::x :x})])

(defn-add-meta f [])

(deftest add-meta-test
  (is (= :x (-> #'f meta ::x))))

;;; # Test validate-errors

;;; With assertions enabled
(alter-var-root #'*validate-errors* (constantly true))

(dfn/def-defn defn-validate-errors-always
  [(validate-errors '(constantly true))])

(defn-validate-errors-always v-e-a
  {:errors [{:type (schema/eq ::fred)}]}
  []
  (throw (ex-info "doesn't match" {:type ::smith})))

(dfn/def-defn defn-validate-errors-never
  [(validate-errors '(constantly false))])

(defn-validate-errors-never v-e-n
  {:errors [{:type (schema/eq ::fred)}]}
  []
  (throw (ex-info "some unkown error" {:type ::smith})))

;;; With assertions disabled
(alter-var-root #'*validate-errors* (constantly nil))

(dfn/def-defn defn-validate-errors-always-off
  [(validate-errors '(constantly true))])

(defn-validate-errors-always-off v-e-a-off
  {:errors [{:type (schema/eq ::fred)}]}
  []
  (throw (ex-info "some unkown error" {:type ::smith})))

(deftest validate-errors-test
  (is (thrown-with-msg?
       clojure.lang.ExceptionInfo #"Error thrown doesn't match :errors schemas"
       (v-e-a)))
  (is (thrown-with-msg?
       clojure.lang.ExceptionInfo #"some unkown error" (v-e-n)))
  (is (thrown-with-msg?
       clojure.lang.ExceptionInfo #"some unkown error" (v-e-a-off))))

(deftest arg-and-ref-test
  (testing "fully specified map"
    (is (= '[{:keys [a b] :as c} c] (arg-and-ref '{:keys [a b] :as c}))))
  (testing "map with no as"
    (let [[arg ref] (arg-and-ref '{:keys [a b]})]
      (is ref)
      (is (= (:as arg) ref))))
  (testing "fully specified vector"
    (is (= '[[a b :as c] c] (arg-and-ref '[a b :as c]))))
  (testing "vector with no as"
    (let [[arg ref] (arg-and-ref '[a b])]
      (is ref)
      (is (= (last arg) ref))))
  (testing "plain symbol"
    (is (= '[a a] (arg-and-ref 'a)))))


;;; # Test validate-arguments
(dfn/def-defn defn-validate-args
  [(validate-arguments)])

;; (defn-validate-args v-arg-compile-error [x] x) ;; should give compile error

(defn-validate-args v-arg-kw
  {:sig [[schema/Keyword schema/Any]]}
  [x]
  x)

(defn-validate-args v-arg-map
  {:sig [[schema/Keyword {schema/Any schema/Any}]]}
  [{:keys [x]}]
  x)

(defn-validate-args v-arg-vec
  {:sig [[schema/Keyword [(schema/one schema/Any "x")]]]}
  [[x]]
  x)

(defn-validate-args v-arg-vararg
  {:sig [[schema/Keyword schema/Any]]}
  [& x]
  (first x))

(defn-validate-args v-arg-map-vararg
  {:sig [[schema/Keyword {schema/Any schema/Any}]]}
  [& {:keys [x]}]
  x)

(defn-validate-args v-arg-vec-vararg
  {:sig [[schema/Keyword (schema/one schema/Any "x")]]}
  [& [x]]
  x)


(deftest validate-args-test
  (testing "simple arg"
    (is (= ::x (v-arg-kw ::x))
        "validates correct return type ok")
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo #"Value does not match schema"
         (v-arg-kw 'a))
        "validates incorrect return type ok"))
  (testing "simple map"
    (is (= ::x (v-arg-map {:x ::x}))
        "validates correct return type ok")
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo #"Value does not match schema"
         (v-arg-map {:x 'a}))
        "validates incorrect return type ok"))
  (testing "simple vector"
    (is (= ::x (v-arg-vec [::x]))
        "validates correct return type ok")
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo #"Value does not match schema"
         (v-arg-vec ['a]))
        "validates incorrect return type ok"))
  (testing "varargs"
    (is (= ::x (v-arg-vararg ::x))
        "validates correct return type ok")
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo #"Value does not match schema"
         (v-arg-vararg 'a))
        "validates incorrect return type ok"))
  (testing "varargs with map destructuring"
    (is (= ::x (v-arg-map-vararg :x ::x))
        "validates correct return type ok")
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo #"Value does not match schema"
         (v-arg-map-vararg :x 'a))
        "validates incorrect return type ok"))
  (testing "varargs with vector destructuring"
    (is (= ::x (v-arg-vec-vararg ::x))
        "validates correct return type ok")
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo #"Value does not match schema"
         (v-arg-vec-vararg 'a))
        "validates incorrect return type ok")))
