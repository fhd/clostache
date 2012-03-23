(ns clostache.test-specs
  (:use clojure.test
        clostache.parser)
  (:require [clj-yaml.core :as yaml]))

(defn- load-spec-tests [spec]
  (let [path (-> (Thread/currentThread)
                 (.getContextClassLoader)
                 (.getResourceAsStream (str "spec/specs/" spec ".yml")))
        data (yaml/parse-string (slurp path))]
    (:tests data)))

(defn- flatten-string [s]
  (.replaceAll (.replaceAll s "\n" "\\\\n") "\r" "\\\\r"))

(defn run-spec-test [spec-test]
  (let [template (:template spec-test)
        data (:data spec-test)
        partials (:partials spec-test)]
    (is (= (:expected spec-test)
           (render template data partials))
        (str (:name spec-test) " - " (:desc spec-test) "\nTemplate: \""
             (flatten-string template) "\"\nData: " data
             (if partials (str "\nPartials: " partials))))))

(defn run-spec-tests [spec]
  (doseq [spec-test (load-spec-tests spec)]
    (run-spec-test spec-test)))

(deftest test-comments
  (run-spec-tests "comments"))

(deftest test-delimiters
  (run-spec-tests "delimiters"))

(deftest test-interpolation
  (run-spec-tests "interpolation"))

(deftest test-sections
  (run-spec-tests "sections"))

(deftest test-inverted
  (run-spec-tests "inverted"))

(deftest test-partials
  (run-spec-tests "partials"))

(deftest test-lambdas
  (run-spec-tests "~lambdas"))
