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

(defn run-spec-test [spec-test]
  (is (= (:expected spec-test)
         (render (:template spec-test) (:data spec-test)))
      (str (:name spec-test) ": " (:desc spec-test))))

(defn run-spec-tests [spec]
  (doseq [spec-test (load-spec-tests spec)]
          (run-spec-test spec-test)))

(deftest test-comments
  (run-spec-tests "comments"))

;; TODO: Test the following specs:
;; - delimiters
;; - interpolation
;; - inverted
;; - sections
;; - partials
