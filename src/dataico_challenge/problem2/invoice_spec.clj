  (ns dataico-challenge.problem2.invoice_spec
    (:gen-class)
    (:require [clojure.java.io :as io]
              [clojure.data.json :as json]
              [clojure.spec.alpha :as s]
              [clojure.string :as str]
              [clojure.spec.alpha :as s]
              )

    )

  (s/def :customer/name string?)
  (s/def :customer/email string?)
  (s/def :invoice/customer (s/keys :req [:customer/name
                                         :customer/email]))

  (s/def :tax/rate double?)
  (s/def :tax/category #{:iva})
  (s/def ::tax (s/keys :req [:tax/category
                             :tax/rate]))
  (s/def :invoice-item/taxes (s/coll-of ::tax :kind vector? :min-count 1))

  (s/def :invoice-item/price double?)
  (s/def :invoice-item/quantity double?)
  (s/def :invoice-item/sku string?)

  (s/def ::invoice-item
    (s/keys :req [:invoice-item/price
                  :invoice-item/quantity
                  :invoice-item/sku
                  :invoice-item/taxes]))

  (s/def :invoice/issue-date inst?)
  (s/def :invoice/items (s/coll-of ::invoice-item :kind vector? :min-count 1))

  (s/def ::invoice
    (s/keys :req [:invoice/issue-date
                  :invoice/customer
                  :invoice/items]))




;PROBLEM 2
; Given the invoice defined in **invoice.json** found in this repo, generate an invoice that passes
; the spec **::invoice** defined in **invoice-spec.clj**. Write a function that as an argument receives
; a file name (a JSON file name in this case) and returns a clojure map such that

;Responsible for reading the invoice file
(defn json->clj [filename]
  ;Open the file
  (with-open [rdr (io/reader filename)]
    ;Get the map from the json
    (json/read rdr :key-fn keyword)))


;Transform an invoice into a valid invoice, fill out invalid or missing spaces if necessary
(defn transform-invoice [data]
  (let [default-taxes [{:tax/category :iva :tax/rate 0.0}]
        default-issue-date (java.util.Date.)]
    (-> data
        ; Ensure customer has required properties
        (update :invoice/customer #(merge {:customer/name "" :customer/email ""} %))
        ; Ensure invoice has required properties
        (update :invoice/items #(if (empty? %) [{:invoice-item/price 0.0 :invoice-item/quantity 0.0 :invoice-item/sku "" :invoice-item/taxes default-taxes}] %))
        ; Ensure each invoice item has required properties
        (update :invoice/items #(mapv (fn [item] (merge {:invoice-item/price 0.0 :invoice-item/quantity 0.0 :invoice-item/sku "" :invoice-item/taxes default-taxes} item)) %))
        ; Ensure issue-date has a valid value
        (update :invoice/issue-date #(or % default-issue-date)))))

;Responsible for getting an invoice from a file name and transforming it into a valid invoice
(defn generate-invoice [filename]
  ;get the clojure map from the file
  (let [data (json->clj filename)
        ;generate a valid invoice from the map
        invoice (transform-invoice data)]
    (if (s/valid? ::invoice invoice)
      ;if the invoice is valid then return it and print success!
      (do (println "Success")
          (println "Invoice formated:")
          (println invoice)
          )
      ;if the invoice is invalid
      (do (println "Error, invalid invoice:")
          (s/explain ::invoice invoice)
          ))))


;Main function
(defn -main
  []
  (println "\nCreating Valid Invoice...\n")

  ;Generate the valid invoice
  (def invoice-formatted (generate-invoice "invoice.json"))

  )