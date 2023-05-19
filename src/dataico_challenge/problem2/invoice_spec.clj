  (ns dataico-challenge.problem2.invoice_spec
    (:gen-class)
    (:require [clojure.java.io :as io]
              [clojure.data.json :as json]
              [clojure.spec.alpha :as s]
              [clojure.string :as str]
              [clojure.spec.alpha :as s]
              )

    )

;New spec for invoices
(require '[clojure.spec.alpha :as s])

(s/def ::id string?)
(s/def ::sku string?)
(s/def ::category keyword?)
(s/def ::rate number?)

(s/def ::tax (s/keys :req-un [::id ::category ::rate]))
(s/def ::retention (s/keys :req-un [::id ::category ::rate]))

(s/def ::taxable (s/keys :opt-un [::taxes]))
(s/def ::retentionable (s/keys :opt-un [::retentions]))

(s/def ::invoice-item (s/merge (s/keys :req-un [::id ::sku]) ::taxable ::retentionable))
(s/def ::invoice (s/keys :req [:invoice/id]  :opt [:invoice/items]))




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

;Responsible for loading the invoice from invoice.edn
(defn load-example-invoice []
  (clojure.edn/read-string (slurp "./invoice.edn"))
  )


;Transform an invoice into a valid invoice, fill out invalid or missing spaces if necessary
(defn transform-invoice [data]
  (let [default-taxes [{:tax/id "" :tax/category :iva :tax/rate 0.0}]
        default-retentions [{:retention/id "" :retention/category :ret_fuente :retention/rate 0.0}]]
    (-> data
        ; Ensure invoice has required properties
        (update :invoice/id #(or % ""))
        ;Ensure there is an items array
        (update :invoice/items #(if (empty? %) [{:invoice-item/id "" :invoice-item/sku ""}] %))
        ; Ensure each invoice item has required properties
        (update :invoice/items #(mapv (fn [item] (merge {:invoice-item/id "" :invoice-item/sku ""}
                                                        (assoc item :taxable/taxes (or (:taxable/taxes item) default-taxes))
                                                        (assoc item :retentionable/retentions (or (:retentionable/retentions item) default-retentions)))) %)))))


;Responsible for getting an invoice from a file name and transforming it into a valid invoice
(defn generate-invoice [filename]
  ;get the clojure map from the file
  (let [data (load-example-invoice)
        ;generate a valid invoice from the map
        invoice (transform-invoice data)]
    (if (s/valid? ::invoice invoice)
      ;if the invoice is valid then return it and print success!
      (do (println "Success")
          (println "Invoice formatted:")
          (println invoice)
          )
      ;if the invoice is invalid
      (do (println "Error, invalid invoice:")
          (println invoice)
          (s/explain ::invoice invoice)
          ))))


;Main function
(defn -main
  []
  (println "\nCreating Valid Invoice...\n")

  ;Generate the valid invoice
  (def invoice-formatted (generate-invoice "invoice.json"))

  )