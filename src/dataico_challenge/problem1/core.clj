(ns dataico-challenge.problem1.core
  (:gen-class))

;PROBLEM 1
; Filter invoices
; Requirements
;   1. At least have one item that has :iva 19%
;   2. At least one item has retention :ret\_fuente 1%
;   3. Every item must satisfy EXACTLY one of the above two conditions. This means that an item cannot have BOTH :iva 19% and retention :ret\_fuente 1%.

;Responisble for checking the XOR operand
(defn xor [a b]
  (not= a b))

;Responsible for filtering the invoices
(defn filter-invoices []
  (let [invoice (clojure.edn/read-string (slurp "./invoice.edn"))]
    (->> invoice
         :invoice/items
         (filter #(xor (some (fn [x] (= 19 (:tax/rate x))) (:taxable/taxes %))
                       (some (fn [x] (= 1 (:retention/rate x))) (:retentionable/retentions %))
                       ))
         )
    )
  )

;;Main function
(defn -main
  [& args]
  (println "\nInvoice items matching requirements:\n")
  (->> (filter-invoices)
       (map println)
       dorun)
  )