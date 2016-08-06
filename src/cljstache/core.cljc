(ns cljstache.core
  "A parser for mustache templates."
  (:require #?(:clj [clojure.java.io :as io])
            [clojure.string :as str :refer [split]]))

;; cljs support
(def re-quote-replacement
  #?(:clj str/re-quote-replacement
     :cljs (fn [s] s)))

;; clj < 1.9 support
#?(:clj
   (def ^Boolean seqable?
     "Returns true if (seq x) will succeed, false otherwise.
      Included in clojure core from v1.9"
     (when (-> "seqable?" symbol resolve)
       seqable?
       (fn [x]
         (or (seq? x)
             (instance? clojure.lang.Seqable x)
             (nil? x)
             (instance? Iterable x)
             (-> x .getClass .isArray)
             (string? x)
             (instance? java.util.Map x))))))

(defn- ^String map-str
  "Apply f to each element of coll, concatenate all results into a
  String."
  [f coll]
  (apply str (map f coll)))

; To match clj regex api
#?(:cljs
   (defn re-matcher [pattern s]
     [(re-pattern pattern) s]))

#?(:cljs
   (defn re-find
     ([re s] (cljs.core/re-find re s))
     ([[re s]] (re-find re s))))

#?(:cljs
   (defn re-groups
     ([[re s]] (.exec re s))))

(defn matcher-find
  ([^java.util.regex.Matcher m] (matcher-find m 0))
  #?(:clj
     ([^java.util.regex.Matcher m offset]
      (when (.find m offset)
        (let [match (.toMatchResult m)]
          {:match-start (.start match)
           :match-end (.end match)}))))
  #?(:cljs
     ([[m s] offset]
      (if-let [match (.exec m (subs s offset))]
        {:match-start (.-index match)
         :match-end (.-lastIndex m)}))))

(defrecord Section [name body start end inverted])

(defn- str-replace
  "Replace all instances of pattern in str"
  [^String s ^Integer from ^Integer to]
  #?(:clj (.replaceAll s from to))
  #?(:cljs (str/replace s from to)))

(defn- replace-all
  "Applies all replacements from the replacement list to the string.
   Replacements are a sequence of two element sequences where the first element
   is the pattern to match and the second is the replacement.
   An optional third boolean argument can be set to true if the replacement
   should not be quoted."
  [string replacements]
  (reduce (fn [string [from to dont-quote]]
            (str-replace (str string) from
                         (if dont-quote
                           to
                           (re-quote-replacement to))))
          string replacements))

(defn- escape-html
  "Replaces angle brackets with the respective HTML entities."
  [string]
  (replace-all string [["&" "&amp;"]
                       ["\"" "&quot;"]
                       ["<" "&lt;"]
                       [">" "&gt;"]]))

(defn- indent-partial
  "Indent all lines of the partial by indent."
  [partial indent]
  (replace-all partial [["(\r\n|[\r\n])(.+)" (str "$1" indent "$2") true]]))

(def regex-chars ["\\" "{" "}" "[" "]" "(" ")" "." "?" "^" "+" "-" "|"])

(defn- escape-regex
  "Escapes characters that have special meaning in regular expressions."
  [regex]
  (replace-all regex (map #(repeat 2 (str "\\" %)) regex-chars)))

(defn- unescape-regex
  "Unescapes characters that have special meaning in regular expressions."
  [regex]
  (replace-all regex (map (fn [char] [(str "\\\\\\" char) char true])
                          regex-chars)))

(defn- ^StringBuilder ->stringbuilder
  ([] (->stringbuilder ""))
  #?(:clj ([^String s] (StringBuilder. s)))
  #?(:cljs ([s] s)))

(defn- ^String sb->str [^StringBuilder s]
  #?(:clj (.toString s))
  #?(:cljs s))

(defn- ^StringBuilder sb-replace
  [^StringBuilder s ^Integer start ^Integer end ^String s']
  #?(:clj (.replace s start end s'))
  #?(:cljs (str (subs s 0 start) s' (subs s end))))

(defn- ^StringBuilder sb-delete
  [^StringBuilder s ^Integer start ^Integer end]
  #?(:clj (.delete s start end))
  #?(:cljs (str (subs s 0 start) (subs s end))))

(defn- ^StringBuilder sb-append
  [^StringBuilder s s']
  #?(:clj (.append s s'))
  #?(:cljs (str s s')))

(defn- ^StringBuilder sb-insert
  [^StringBuilder s ^Integer index ^StringBuilder s']
  #?(:clj (.insert s index s'))
  #?(:cljs (sb-replace s index index s')))

(defn- process-set-delimiters
  "Replaces custom set delimiters with mustaches."
  [^String template data]
  (let [builder (->stringbuilder template)
        data (atom data)
        open-delim (atom "\\{\\{")
        close-delim (atom "\\}\\}")
        set-delims (fn [open close]
                     (doseq [[var delim]
                             [[open-delim open] [close-delim close]]]
                       (swap! var (constantly (escape-regex delim)))))]
    (loop [offset 0]
      (let [string (sb->str builder)
            custom-delim (not (= "\\{\\{" @open-delim))
            matcher (re-matcher
                     (re-pattern (str "(" @open-delim ".*?" @close-delim
                                      (if custom-delim
                                        (str "|\\{\\{.*?\\}\\}"))
                                      ")"))
                     string)]
        (when-let [match-result (matcher-find matcher offset)]
          (let [{:keys [match-start match-end]} match-result
                match (subs string match-start match-end)]
            (if (and custom-delim (= "{{" (subs match 0 2)))
              (if-let [tag (re-find #"\{\{(.*?)\}\}" match)]
                (do
                  (sb-replace builder match-start match-end
                            (str "\\{\\{" (second tag) "\\}\\}"))
                  (recur (int match-end))))
              (if-let [delim-change (re-find
                                     (re-pattern (str @open-delim
                                                      "=\\s*(.*?) (.*?)\\s*="
                                                      @close-delim))
                                     match)]
                (do
                  (apply set-delims (rest delim-change))
                  (sb-delete builder match-start match-end)
                  (recur (int match-start)))
                (if-let [tag (re-find
                              (re-pattern (str @open-delim "(.*?)"
                                               @close-delim))
                              match)]
                  (let [section-start (re-find (re-pattern
                                                (str "^"
                                                     @open-delim
                                                     "\\s*#\\s*(.*?)\\s*"
                                                     @close-delim))
                                               (first tag))
                        key (if section-start (keyword (second section-start)))
                        value (if key (key @data))]
                    (if (and value (fn? value)
                             (not (and (= @open-delim "\\{\\{")
                                       (= @close-delim "\\}\\}"))))
                      (swap! data
                             #(update-in % [key]
                                         (fn [old]
                                           (fn [data]
                                             (str "{{="
                                                  (unescape-regex @open-delim)
                                                  " "
                                                  (unescape-regex @close-delim)
                                                  "=}}"
                                                  (old data)))))))
                    (sb-replace builder match-start match-end
                              (str "{{" (second tag) "}}"))
                    (recur (int match-end))))))))))
    [(sb->str builder) @data]))

(defn- create-partial-replacements
  "Creates pairs of partial replacements."
  [template partials]
  (apply concat
         (for [k (keys partials)]
           (let [regex (re-pattern (str "(\r\n|[\r\n]|^)([ \\t]*)\\{\\{>\\s*"
                                        (name k) "\\s*\\}\\}"))
                 indent (nth (first (re-seq (re-pattern regex) template)) 2)]
             [[(str "\\{\\{>\\s*" (name k) "\\s*\\}\\}")
               (first (process-set-delimiters (indent-partial (str (k partials))
                                                              indent) {}))]]))))

(defn- include-partials
  "Include partials within the template."
  [template partials]
  (replace-all template (create-partial-replacements template partials)))

(defn- remove-comments
  "Removes comments from the template."
  [template]
  (let [comment-regex "\\{\\{\\![^\\}]*\\}\\}"]
    (replace-all template [[(str "(^|[\n\r])[ \t]*" comment-regex
                                 "(\r\n|[\r\n]|$)") "$1" true]
                           [comment-regex ""]])))
#?(:clj
   (defn- next-index
     "Return the next index of the supplied regex."
     ([section regex] (next-index section regex 0))
     ([^String section regex index]
      (if (= index -1)
        -1
        (let [s (.substring section index)
              matcher (re-matcher regex s)]
          (if (nil? (re-find matcher))
            -1
            (+ index (.start (.toMatchResult matcher)))))))))

#?(:cljs
   (defn- next-index
     "Return the next index of the supplied regex."
     ([section regex] (next-index section regex 0))
     ([^String section regex index]
      (if (= index -1)
        -1
        (let [s (subs section index)
              matcher (js/RegExp. (.-source (str regex)) "g")]
          (if-let [m (.exec regex s)]
            (+ index (.-index m))
            -1))))))

(defn- find-section-start-tag
  "Find the next section start tag, starting to search at index."
  [^String template index]
  (next-index template #"\{\{[#\^]" index))

(defn- find-section-end-tag
  "Find the matching end tag for a section at the specified level,
   starting to search at index."
  [^String template ^long index ^long level]
  (let [next-start (find-section-start-tag template index)
        next-end (.indexOf ^String template "{{/" index)]
    (if (= next-end -1)
      -1
      (if (and (not (= next-start -1)) (< next-start next-end))
        (find-section-end-tag template (+ next-start 3) (inc level))
        (if (= level 1)
          next-end
          (find-section-end-tag template (+ next-end 3) (dec level)))))))

(defn- extract-section
  "Extracts the outer section from the template."
  [^String template]
  (let [^Long start (find-section-start-tag template 0)]
    (if (= start -1)
      nil
      (let [inverted (= (str (.charAt template (+ start 2))) "^")
            ^Long end-tag (find-section-end-tag template (+ start 3) 1)]
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

(defn- replace-all-callback
  "Replaces each occurrence of the regex with the return value of the callback."
  [^String string regex callback]
  (str/replace string regex #(callback %)))

(declare render-template)

(defn replace-variables
  "Replaces variables in the template with their values from the data."
  [template data partials]
  (let [regex #"\{\{(\{|\&|\>|)\s*(.*?)\s*\}{2,3}"]
    (replace-all-callback template regex
                          #(let [var-name (nth % 2)
                                 var-k (keyword var-name)
                                 var-type (second %)
                                 var-value (var-k data)
                                 var-value (if (fn? var-value)
                                             (render-template
                                              (var-value)
                                              (dissoc data var-name)
                                              partials)
                                             var-value)
                                 var-value (str var-value)]
                             (cond (= var-type "") (escape-html var-value)
                                   (= var-type ">") (render-template (var-k partials) data partials)
                                   :else var-value)))))

(defn- join-standalone-delimiter-tags
  "Remove newlines after standalone (i.e. on their own line) delimiter tags."
  [template]
  (replace-all
   template
   (let [eol-start "(\r\n|[\r\n]|^)"
         eol-end "(\r\n|[\r\n]|$)"]
     [[(str eol-start "[ \t]*(\\{\\{=[^\\}]*\\}\\})" eol-end) "$1$2"
       true]])))

(defn- path-data
  "Extract the data for the supplied path."
  [elements data]
  (get-in data (map keyword elements)))

(defn- convert-path
  "Convert a tag with a dotted name to nested sections, using the
  supplied delimiters to access the value."
  [tag open-delim close-delim data]
  (let [tag-type (last open-delim)
        section-tag (some #{tag-type} [\# \^ \/])
        section-end-tag (= tag-type \/)
        builder (->stringbuilder)
        tail-builder (if section-tag nil (->stringbuilder))
        elements (split tag #"\.")
        element-to-invert (if (= tag-type \^)
                            (loop [path [(first elements)]
                                   remaining-elements (rest elements)]
                              (if (not (empty? remaining-elements))
                                (if (nil? (path-data path data))
                                  (last path)
                                  (recur (conj path (first remaining-elements))
                                         (next remaining-elements))))))]
    (if (and (not section-tag) (nil? (path-data elements data)))
      ""
      (let [elements (if section-end-tag (reverse elements) elements)]
        (do
          (doseq [element (butlast elements)]
            (sb-append builder (str "{{" (if section-end-tag "/"
                                           (if (= element element-to-invert)
                                             "^" "#"))
                                  element "}}"))
            (if (not (nil? tail-builder))
              (sb-insert tail-builder 0 (str "{{/" element "}}"))))
          (sb-append builder (str open-delim (last elements) close-delim))
          (str (sb->str builder) (if (not (nil? tail-builder))
                                     (sb->str tail-builder))))))))

(defn- convert-paths
  "Converts tags with dotted tag names to nested sections."
  [^String template data]
  (loop [^String s ^String template]
    (let [matcher (re-matcher #"(\{\{[\{&#\^/]?)([^\}]+\.[^\}]+)(\}{2,3})" s)]
      (if-let [match-result (matcher-find matcher)]
        (let [{:keys [match-start match-end]} match-result
              groups (re-groups matcher)
              converted (convert-path (str/trim (nth groups 2)) (nth groups 1)
                                      (nth groups 3) data)]
          (recur (str (subs s 0 match-start) converted
                      (subs s match-end))))
        s))))

(defn- join-standalone-tags
  "Remove newlines after standalone (i.e. on their own line) section/partials
   tags."
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
       true]
      [(str eol-start "([ \t]*\\{\\{>\\s*[^\\}]*\\s*\\}\\})" eol-end) "$1$2"
       true]])))

(defn- preprocess
  "Preprocesses template and data (e.g. removing comments)."
  [template data partials]
  (let [template (join-standalone-delimiter-tags template)
        [template data] (process-set-delimiters template data)
        template (join-standalone-tags template)
        template (remove-comments template)
        template (include-partials template partials)
        template (convert-paths template data)]
    [template data]))

(defn- render-section
  [section data partials]
  (let [section-data ((keyword (:name section)) data)]
    (if (:inverted section)
      (if (or (and (seqable? section-data) (empty? section-data))
              (not section-data))
        (:body section))
      (if section-data
        (if (fn? section-data)
          (let [result (section-data (:body section))]
            (if (fn? result)
              (result #(render-template % data partials))
              result))
          (let [section-data (cond (sequential? section-data) section-data
                                   (map? section-data) [section-data]
                                   (seqable? section-data) (seq section-data)
                                   :else [{}])
                section-data (if (map? (first section-data))
                               section-data
                               (map (fn [e] {(keyword ".") e})
                                    section-data))
                section-data (map #(conj data %) section-data)]
            (map-str (fn [m]
                       (render-template (:body section) m partials))
                     section-data)))))))

(defn- render-template
  "Renders the template with the data and partials."
  [^String template data partials]
  (let [[^String template data] (preprocess template data partials)
        ^String section (extract-section template)]
    (if (nil? section)
      (replace-variables template data partials)
      (let [before (.substring template 0 (:start section))
            after (.substring template (:end section))]
        (recur (str before (render-section section data partials) after) data
               partials)))))

(defn render
  "Renders the template with the data and, if supplied, partials."
  ([template]
     (render template {} {}))
  ([template data]
     (render template data {}))
  ([template data partials]
     (replace-all (render-template template data partials)
                  [["\\\\\\{\\\\\\{" "{{"]
                   ["\\\\\\}\\\\\\}" "}}"]])))

#?(:clj
   (defn render-resource
     "Renders a resource located on the classpath"
     ([^String path]
      (render (slurp (io/resource path)) {}))
     ([^String path data]
      (render (slurp (io/resource path)) data))
     ([^String path data partials]
      (render (slurp (io/resource path)) data partials))))
