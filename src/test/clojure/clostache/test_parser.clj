(ns clostache.test-parser
  (:use clojure.test
        clostache.parser))

(deftest test-render-simple
  (is (= "Hello, Felix" (render "Hello, {{name}}" {:name "Felix"}))))

(deftest test-render-multi-line
  (is (= "Hello\nFelix" (render "Hello\n{{name}}" {:name "Felix"}))))

(deftest test-render-html-unescaped
  (is (= "&\"<>"
         (render "{{{string}}}" {:string "&\\\"<>"}))))

(deftest test-render-html-unescaped-ampersand
  (is (= "&\"<>"
         (render "{{&string}}" {:string "&\"<>"}))))

(deftest test-render-html-escaped
  (is (= "&amp;&quot;&lt;&gt;"
         (render "{{string}}" {:string "&\"<>"}))))

(deftest test-render-list
  (is (= "Hello, Felix, Jenny!" (render "Hello{{#names}}, {{name}}{{/names}}!"
                                        {:names [{:name "Felix"}
                                                 {:name "Jenny"}]}))))
(deftest test-render-list-twice
  (is (= "Hello, Felix, Jenny! Hello, Felix, Jenny!"
         (render (str "Hello{{#names}}, {{name}}{{/names}}! "
                      "Hello{{#names}}, {{name}}{{/names}}!")
                 {:names [{:name "Felix"} {:name "Jenny"}]}))))

(deftest test-render-seq
  (is (= "Hello, Felix, Jenny!" (render "Hello{{#names}}, {{name}}{{/names}}!"
                                        {:names (seq [{:name "Felix"}
                                                 {:name "Jenny"}])}))))
(deftest test-render-hash
  ; according to mustache(5) non-false, non-list value
  ; should be used as a context for a single rendering of a block
  (is (= "Hello, Felix!" (render "Hello{{#person}}, {{name}}{{/person}}!"
                                        {:person {:name "Felix"}}))))

(deftest test-render-empty-list
  (is (= "" (render "{{#things}}Something{{/things}}" {:things []}))))

(deftest test-render-comment
  (is (= "Hello, Felix!" (render "Hello, {{! This is a comment.}}{{name}}!"
                                 {:name "Felix"}))))

(deftest test-render-tags-with-whitespace
  (is (= "Hello, Felix" (render "Hello, {{# names }}{{ name }}{{/ names }}"
                                {:names [{:name "Felix"}]}))))

(deftest test-render-boolean-true
  (is (= "Hello, Felix" (render "Hello, {{#condition}}Felix{{/condition}}"
                                {:condition true}))))

(deftest test-render-boolean-false
  (is (= "Hello, " (render "Hello, {{#condition}}Felix{{/condition}}"
                           {:condition false}))))

(deftest test-render-inverted-empty-list
  (is (= "Empty" (render "{{^things}}Empty{{/things}}" {:things []}))))

(deftest test-render-inverted-list
  (is (= "" (render "{{^things}}Empty{{/things}}" {:things ["Something"]}))))

(deftest test-render-inverted-boolean-true
  (is (= "Hello, " (render "Hello, {{^condition}}Felix{{/condition}}"
                           {:condition true}))))

(deftest test-render-inverted-boolean-false
  (is (= "Hello, Felix" (render "Hello, {{^condition}}Felix{{/condition}}"
                                {:condition false}))))
