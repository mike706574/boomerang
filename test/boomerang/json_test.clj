(ns boomerang.json-test
  (:require [boomerang.json :as json]
            [clojure.test :refer :all]))

(def example "{\"foo\\/bar\":\"baz\"}")

(deftest reading-from-a-reader
  (testing "reading from a reader"
    (is (= {:foo/bar "baz"} (json/read (java.io.StringReader. example))))))

(deftest reading-from-astring
  (testing "reading from string"
    (is (= {:foo/bar "baz"} (json/read-str example)))))

(deftest writing-to-a-string
  (testing "writing to a string"
    (is (= example (json/write-str {:foo/bar "baz"})))))
