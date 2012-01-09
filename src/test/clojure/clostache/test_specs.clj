(ns clostache.test-specs
  (:use clojure.test
        clostache.parser)
  (:require [clj-yaml.core :as yaml]))

;; TODO: Print a warning instead of an exception when the specs are missing

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
        data (:data spec-test)]
    (is (= (:expected spec-test)
           (render template data))
        (str (:name spec-test) " - " (:desc spec-test) "\nTemplate: \""
             (flatten-string template) "\"\nData: " data))))

(defn run-spec-tests [spec]
  (doseq [spec-test (load-spec-tests spec)]
          (run-spec-test spec-test)))

(deftest test-comments
  (run-spec-tests "comments"))

;; TODO: Uncomment these when partials are implemented
;; (deftest test-delimiters
;;   (run-spec-tests "delimiters"))

(deftest test-interpolation
  (run-spec-tests "interpolation"))

;; TODO: Test the following specs:
;; - inverted
;; - sections
;; - partials
