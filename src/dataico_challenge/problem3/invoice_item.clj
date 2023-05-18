(ns dataico-challenge.problem3.invoice-item
  (:require [clojure.test :refer :all]
            [dataico-challenge.problem3.invoice-item :refer :all])
  )

(defn- discount-factor [{:invoice-item/keys [discount-rate]
                         :or                {discount-rate 0}}]
  (- 1 (/ discount-rate 100.0)))

(defn subtotal
  [{:invoice-item/keys [precise-quantity precise-price discount-rate]
    :as                item
    :or                {discount-rate 0}}]
  (* precise-price precise-quantity (discount-factor item)))


;PROBLEM 3
;Given the function **subtotal** defined in **invoice-item.clj** in this repo, write at least five tests using
; clojure core **deftest** that demonstrates its correctness. This subtotal function calculates the subtotal of
; an invoice-item taking a discount-rate into account. Make sure the tests cover as many edge cases as you can!


(deftest test-subtotal
  (testing "Subtotal with no discount"
    (is (= (subtotal {:invoice-item/precise-quantity 2 :invoice-item/precise-price 10}) 20.0)))
  (testing "Subtotal with discount"
    (is (= (subtotal {:invoice-item/precise-quantity 2 :invoice-item/precise-price 10 :invoice-item/discount-rate 50}) 10.0)))
  (testing "Subtotal with zero quantity"
    (is (= (subtotal {:invoice-item/precise-quantity 0 :invoice-item/precise-price 10}) 0.0)))
  (testing "Subtotal with zero price"
    (is (= (subtotal {:invoice-item/precise-quantity 2 :invoice-item/precise-price 0}) 0.0)))
  (testing "Subtotal with zero quantity and price"
    (is (= (subtotal {:invoice-item/precise-quantity 0 :invoice-item/precise-price 0}) 0.0))))


; Run all tests in te namespace
(run-tests)