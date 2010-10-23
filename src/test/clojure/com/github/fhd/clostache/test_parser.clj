(ns com.github.fhd.clostache.test-parser
  (:use clojure.test
        com.github.fhd.clostache.parser))

(deftest test-render-simple
  (is (= "Hello, Felix" (render "Hello, {{name}}" {:name "Felix"}))))

(deftest test-render-multi-line
  (is (= "Hello\nFelix" (render "Hello\n{{name}}" {:name "Felix"}))))

(deftest test-render-html-unescaped
  (is (= "<h1>Heading</h1>"
         (render "{{{heading}}}" {:heading "<h1>Heading</h1>"}))))

(deftest test-render-html-unescaped-ampersand
  (is (= "<h1>Heading</h1>"
         (render "{{&heading}}" {:heading "<h1>Heading</h1>"}))))

(deftest test-render-html-escaped
  (is (= "&lt;h1&gt;Heading&lt;/h1&gt;"
         (render "{{heading}}" {:heading "<h1>Heading</h1>"}))))

(deftest test-render-simple-list
  (is (= "Hello, Felix, Jenny!" (render "Hello{{#names}}, {{name}}{{/names}}!"
                                        {:names [{:name "Felix"}
                                                 {:name "Jenny"}]}))))

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
