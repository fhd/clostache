(ns clostache.parser
  "A parser for mustache templates."
  (:use [clojure.contrib.string :only (map-str split)])
  (:import java.util.regex.Matcher))

(defrecord Section [name body start end inverted])

(defn- replace-all
  "Applies all replacements from the replacement list to the string."
  [string replacements]
  (reduce (fn [string [from to dont-quote]]
            (.replaceAll string from
                         (if dont-quote to (Matcher/quoteReplacement to))))
          string replacements))

(defn- escape-html
  "Replaces angle brackets with the respective HTML entities."
  [string]
  (replace-all string [["&" "&amp;"]
                       ["\"" "&quot;"]
                       ["<" "&lt;"]
                       [">" "&gt;"]]))

(defn- create-variable-replacements
  "Creates pairs of variable replacements from the data."
  [data]
  (apply concat
         (for [k (keys data)]
           (let [var-name (name k)
                 var-value (str (k data))]
             [[(str "\\{\\{\\{\\s*" var-name "\\s*\\}\\}\\}") var-value]
              [(str "\\{\\{\\&\\s*" var-name "\\s*\\}\\}") var-value]
              [(str "\\{\\{\\s*" var-name "\\s*\\}\\}")
               (escape-html var-value)]]))))

(defn- remove-comments
  "Removes comments from the template."
  [template]
  (let [comment-regex "\\{\\{\\![^\\}]*\\}\\}"]
    (replace-all template [[(str "(^|[\n\r])[ \t]*" comment-regex
                                 "(\r\n|[\r\n]|$)") "$1" true]
                           [comment-regex ""]])))

(defn- next-index
  "Return the next index of the supplied regex."
  ([section regex]
     (next-index section regex 0))
  ([section regex index]
     (if (= index -1)
       -1
       (let [s (.substring section index)
             matcher (re-matcher regex s)]
         (if (nil? (re-find matcher))
           -1
           (+ index (.start (.toMatchResult matcher))))))))

(defn- find-section-start-tag
  "Find the next section start tag, starting to search at index."
  [template index]
  (next-index template #"\{\{[#\^]" index))

(defn- find-section-end-tag
  "Find the matching end tag for a section at the specified level,
   starting to search at index."
  [template index level]
  (let [next-start (find-section-start-tag template index)
        next-end (.indexOf template "{{/" index)]
    (if (= next-end -1)
      -1
      (if (and (not (= next-start -1)) (< next-start next-end))
        (find-section-end-tag template (+ next-start 3) (inc level))
        (if (= level 1)
          next-end
          (find-section-end-tag template (+ next-end 3) (dec level)))))))

(defn- extract-section
  "Extracts the outer section from the template."
  [template]
  (let [start (find-section-start-tag template 0)]
    (if (= start -1)
      nil
      (let [inverted (= (str (.charAt template (+ start 2))) "^")
            end-tag (find-section-end-tag template (+ start 3) 1)]
        (if (= end-tag -1)
          nil
          (let [end (+ (.indexOf template "}}" end-tag) 2)
                section (.substring template start end)
                body-start (+ (.indexOf section "}}") 2)
                body-end (.lastIndexOf section "{{")
                body (if (or (= body-start -1) (= body-end -1)
                             (< body-end body-start))
                       ""
                       (.substring section body-start body-end))
                section-name (.trim (.substring section 3
                                                (.indexOf section "}}")))]
            (Section. section-name body start end inverted)))))))

(defn- remove-all-tags
  "Removes all tags from the template."
  [template]
  (replace-all template [["\\{\\{\\S*\\}\\}" ""]]))

(defn- escape-regex
  "Escapes characters that have special meaning in regular expressions."
  [regex]
  (let [chars-to-escape ["\\" "{" "}" "[" "]" "(" ")" "." "?" "^" "+" "-" "|"]]
    (replace-all regex (map #(repeat 2 (str "\\" %)) chars-to-escape))))

(defn- process-set-delimiters
  "Replaces custom set delimiters with mustaches."
  [template]
  (let [builder (StringBuilder. template)
        open-delim (atom "\\{\\{")
        close-delim (atom "\\}\\}")
        set-delims (fn [open close]
                     (doseq [[var delim]
                             [[open-delim open] [close-delim close]]]
                       (swap! var (constantly (escape-regex delim)))))]
    (loop [offset 0]
      (let [string (.toString builder)
            matcher (re-matcher
                     (re-pattern (str @open-delim ".*?" @close-delim))
                     string)]
        (if (.find matcher offset)
          (let [match-result (.toMatchResult matcher)
                match-start (.start match-result)
                match-end (.end match-result)
                match (.substring string match-start match-end)]
            (if-let [delim-change (re-find
                                   (re-pattern (str @open-delim "=(.*?) (.*?)="
                                                    @close-delim))
                                   match)]
              (do
                (apply set-delims (rest delim-change))
                (.delete builder match-start match-end)
                (recur match-start))
              (if-let [tag (re-find
                            (re-pattern (str @open-delim "(.*?)" @close-delim))
                            match)]
                (do
                  (.replace builder match-start match-end
                            (str "{{" (second tag) "}}"))
                  (recur match-end))))))))
  (.toString builder)))

(defn- path-data
  "Extract the data for the supplied path"
  [elements data]
  (loop [i 0
         d data]
    (let [element (nth elements i)
          value ((keyword element) d)]
      (if (or (nil? value))
        nil
        (let [next (inc i)]
          (if (= next (count elements))
            value
            (recur next value)))))))

(defn- convert-path
  "Convert a tag with a dotted name to nested sections, using the
  supplied delimiters to access the value."
  [tag open-delim close-delim data]
  (let [builder (StringBuilder.)
        tail-builder (StringBuilder.)
        elements (split #"\." tag)]
    (if (nil? (path-data elements data))
      ""
      (do
        (doseq [element (butlast elements)]
          (.append builder (str "{{#" element "}}"))
          (.insert tail-builder 0 (str "{{/" element "}}")))
        (.append builder (str open-delim (last elements) close-delim))
        (str (.toString builder) (.toString tail-builder))))))

(defn- convert-paths
  "Converts tags with dotted tag names to nested sections."
  [template data]
  (loop [s template]
    (let [matcher (re-matcher #"(\{\{[\{&]?)([^\}]+\.[^\}]+)(\}{2,3})" s)]
      (if-let [match (re-find matcher)]
        (let [match-start (.start matcher)
              match-end (.end matcher)
              converted (convert-path (nth match 2) (nth match 1)
                                      (nth match 3) data)]
          (recur (str (.substring s 0 match-start) converted
                      (.substring s match-end))))
        s))))

(defn- join-standalone-section-tags
  "Remove newlines after standalone (i.e. on their own line) section tags."
  [template]
  (replace-all
   template
   (let [eol-start "(\r\n|[\r\n]|^)"
         eol-end "(\r\n|[\r\n]|$)"]
     [[(str eol-start
            "\\{\\{[#\\^][^\\}]*\\}\\}(\r\n|[\r\n])\\{\\{/[^\\}]*\\}\\}"
            eol-end)
       "$1" true]
      [(str eol-start "[ \t]*(\\{\\{[#\\^/][^\\}]*\\}\\})" eol-end) "$1$2"
       true]])))

(defn- preprocess
  "Preprocesses the template (e.g. removing comments)."
  [template data]
  (convert-paths (remove-comments
                  (process-set-delimiters
                   (join-standalone-section-tags template)))
                 data))

(defn render
  "Renders the template with the data."
  [template data]
  (let [replacements (create-variable-replacements data)
        template (preprocess template data)
        section (extract-section template)]
    (if (nil? section)
      (remove-all-tags (replace-all template replacements))
      (let [before (.substring template 0 (:start section))
            after (.substring template (:end section))
            section-data ((keyword (:name section)) data)]
        (recur
         (str before
              (if (:inverted section)
                (if (or (and (vector? section-data) (empty? section-data))
                        (not section-data))
                  (:body section))
                (if section-data
                  (let [section-data (if (or (sequential? section-data)
                                             (map? section-data))
                                       section-data {})
                        section-data (if (sequential? section-data) section-data
                                         [section-data])
                        section-data (map #(conj data %) section-data)]
                    (map-str (fn [m]
                               (render (:body section) m)) section-data))))
              after) data)))))
