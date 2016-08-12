(ns ordinator.catalogue
  (:use [dk.ative.docjure.spreadsheet] :reload-all)
  (:require [clojure.string :as s]))

(def cols {:B :codestr :C :description :D :origin :F :packsize :G :price :H :vat})

(def raw-cat-file "resources/Essential_Stock_File.xls")
(def stock-sheet-name "Stock_Full")

(def catalogue-data (atom {}))

(defn read-cat-file
  []
  (let [wb (load-workbook raw-cat-file)
        sheet (select-sheet stock-sheet-name wb)]
    (select-columns cols sheet)))

(defn get-units [packsize]
  (let [regx #"^(\d+)(\s*([*x]).*)$"
        sixpack (s/trim packsize)
        matches (re-seq regx sixpack)]
    (if-let [matches (first matches)]
      {:unitsperpack (Integer. (second matches))
       :unit (str "1" (nth matches 2))
       :splits? (= (nth matches 3) "*")}
      {:unitsperpack 1
       :unit sixpack
       :splits? false})))

(defn update-catalogue
  []
  (let [raw-data (read-cat-file)]
    (prn "raw-cat-data" raw-data)
    (->> raw-data
         (drop 1)
         (map (fn [{:keys [packsize] :as m}] (merge m (get-units packsize))))
         (reduce (fn [m {:keys [codestr] :as line}]
                   (let [codekey (-> codestr s/trim s/lower-case keyword)]
                     (assoc m codekey line))) {})
         (reset! catalogue-data))))

(defn get-catalogue [] (prn "get-cat") @catalogue-data)

(defn get-catalogue-item
  [code]
  (code @catalogue-data))
