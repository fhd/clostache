(ns com.github.fhd.clostache.parser
  "A parser for mustache templates."
  (:use [clojure.contrib.string :only (map-str)]))

(defrecord Section [name body start end])

(defn- replace-all
  "Applies all replacements from the replacement list to the string."
  [string replacements]
  (reduce (fn [string [from to]]
            (.replaceAll string from to)) string replacements))

(defn- escape-html
  "Replaces angle brackets with the respective HTML entities."
  [string]
  (replace-all string [["<" "&lt;"] [">" "&gt;"]]))

(defn- create-variable-replacements
  "Creates pairs of variable replacements from the data."
  [data]
  (apply concat
         (for [k (keys data)]
           (let [var-name (name k)
                 var-value (k data)]
             (if (instance? String var-value)
               [[(str "\\{\\{\\{" var-name "\\}\\}\\}") var-value]
                [(str "\\{\\{" var-name "\\}\\}")
                 (escape-html var-value)]])))))

(defn- extract-section
  "Extracts the outer section from the template."
  [template]
  (let [start (.indexOf template "{{#")
        end-tag (.indexOf template "{{/" start)
        end (+ (.indexOf template "}}" end-tag) 2)]
    (if (or (= start -1) (= end 1))
      nil
      (let [section (.substring template start end)
            body-start (+ (.indexOf section "}}") 2)
            body-end (.lastIndexOf section "{{")
            body (.substring section body-start body-end)
            section-name (.substring section 3 (- body-start 2))]
        (Section. section-name body start end)))))

(defn render
  "Renders the template with the data."
  [template data]
  (let [replacements (create-variable-replacements data)
        section (extract-section template)]
    (if (nil? section)
      (replace-all template replacements)
      (let [before (.substring template 0 (:start section))
            after (.substring template (:end section))
            section-data ((keyword (:name section)) data)]
        (str (replace-all before replacements)
             (map-str (fn [m] (render (:body section) m)) section-data)
             (replace-all after replacements))))))
