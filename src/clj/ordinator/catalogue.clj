(ns ordinator.catalogue
  (:use [dk.ative.docjure.spreadsheet] :reload-all))

(def cols {:B :code :C :description :D :origin :F :packsize :G :price :H :vat})

(def raw-cat-file "resources/Essential_Stock_File_test.xls")
(def stock-sheet-name "Stock_Full")

(def catalogue-data (atom {}))

(defn read-cat-file
  []
  (let [wb (load-workbook raw-cat-file)
        sheet (select-sheet stock-sheet-name wb)]
    (select-columns cols sheet)))

;TODO add Albany units and splittable flag
(defn update-catalogue
  []
  (let [raw-data (read-cat-file)]
    (->> raw-data
         (drop 1)
         (reduce (fn [m {:keys [code] :as line}] (assoc m code line)) {})
         (reset! catalogue-data))))

(defn get-catalogue [] @catalogue-data)
