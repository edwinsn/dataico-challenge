(ns dataico-challenge.problem2.invoice_spec
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [clojure.spec.alpha :as s]
            [clojure.instant :as i]
            [clojure.instant :as instant]
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

(defn convert-date-format [date-str]
  (let [[day month year] (clojure.string/split date-str #"/")]
    (str year "-" month "-" day)
    )
  )

;Responsible for reading the invoice file
(defn json->clj [filename]
  (with-open [rdr (io/reader filename)]
    (-> (json/read rdr :key-fn (fn [k] (keyword (clojure.string/replace k "_" "-"))))
        (update :invoice
                (fn [invoice]
                  (reduce-kv
                    (fn [acc k v]
                      (if (namespace k)
                        (assoc acc k v)
                        (assoc acc (keyword "invoice" (name k)) v)))
                    {}
                    invoice)))
        )))


;Transform an invoice into a valid invoice, fill out invalid or missing spaces if necessary
(defn transform-invoice [data]

  (-> data
      ;Format customers
      (update :invoice/customer  #(if % {:customer/email (:email %) :customer/name (:name %)} ))
      ;Format the issue-date
      (update :invoice/issue-date #(if %  (instant/read-instant-date (convert-date-format %)) ))
      ;Format the items
      (update :invoice/items
              (fn [invoice-items]
                (mapv
                  #(if % {:invoice-item/price (:price %)
                          :invoice-item/quantity (:quantity %)
                          :invoice-item/sku (:sku %)
                          ;Format items taxes
                          :invoice-item/taxes (mapv
                                   (fn [tax] (if tax {
                                                      :tax/category (and (= (:tax-category tax) "IVA") :iva)
                                                      :tax/rate (double (:tax-rate tax ))
                                                      }) )
                            (:taxes %)
                            )
                          } )
                  invoice-items
                  )
                )
              )
      )
  )

;Responsible for getting an invoice from a file name and transforming it into a valid invoice
(defn generate-invoice [filename]
  ;get the clojure map from the file
  (let [data (json->clj filename)
        ;generate a valid invoice from the map
        invoice (transform-invoice (:invoice data))]
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