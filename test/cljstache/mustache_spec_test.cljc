(ns cljstache.mustache-spec-test
  "Test against the [Mustache spec](http://github.com/mustache/spec)"
  (:require [cljstache.core :refer [render]]
            [clojure.string :as str]
            #?(:clj [clojure.test :refer :all])
            #?(:cljs [cljs.test :refer-macros [deftest testing is]])
            #?(:clj [clojure.data.json :as json]))
  #?(:cljs (:require-macros [cljstache.mustache-spec-test :refer [load-specs]])))

;; We load the specs at compile time via macro
;; for clojurescript compatibility

(def specs ["comments" "delimiters" "interpolation" "sections" "inverted" "partials" "~lambdas"])

(defn- spec-path [spec] (str "test-resources/spec/specs/" spec ".json"))

#?(:clj (defn- load-spec-tests [spec]
          (-> spec spec-path slurp json/read-json :tests)))

#?(:clj (defmacro load-specs []
          (into {} (for [spec specs] [spec (load-spec-tests spec)]))))

(def spec-tests (load-specs))

(defn- update-lambda-in [data f]
  (if (contains? data :lambda)
    (update-in data [:lambda] f)
    data))

(defn- extract-lambdas [data]
  (update-lambda-in data #(:clojure %)))

#?(:cljs (def load-string identity))

(defn- load-lambdas [data]
  (update-lambda-in data #(load-string %)))

(defn- flatten-string [^String s]
  (str/replace (str/replace s "\n" "\\\\n") "\r" "\\\\r"))

(defn run-spec-test [spec-test]
  (let [template (:template spec-test)
        readable-data (extract-lambdas (:data spec-test))
        data (load-lambdas readable-data)
        partials (:partials spec-test)]
    (is (= (:expected spec-test)
           (render template data partials))
        (str (:name spec-test) " - " (:desc spec-test) "\nTemplate: \""
             (flatten-string template) "\"\nData: " readable-data
             (if partials (str "\nPartials: " partials))))))

(defn run-spec-tests [spec]
  (doseq [spec-test (spec-tests spec)]
    (run-spec-test spec-test)))

;; OutOfMemoryError in cljs
#?(:clj
   (deftest test-comments
     (run-spec-tests "comments")))

;; Currently fails in cljs
(deftest test-delimiters
  (run-spec-tests "delimiters"))

;; OutOfMemoryError in cljs
#?(:clj
   (deftest test-interpolation
     (run-spec-tests "interpolation")))

;; OutOfMemoryError in cljs
#?(:clj
   (deftest test-sections
     (run-spec-tests "sections")))

;; OutOfMemoryError in cljs
#?(:clj
   (deftest test-inverted
     (run-spec-tests "inverted")))

;; Currently fails in cljs
(deftest test-partials
  (run-spec-tests "partials"))

;; Lambdas are not supported in Clojurescript due to lack of `eval'
;; http://blog.fogus.me/2011/07/29/compiling-clojure-to-javascript-pt-2-why-no-eval/
#?(:clj
   (deftest test-lambdas
     (run-spec-tests "~lambdas")))
