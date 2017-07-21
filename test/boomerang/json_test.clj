(ns boomerang.json-test
  (:require [boomerang.json :as json]
            [clojure.test :refer :all]))

(def example "{\"foo\\/bar\":\"baz\"}")

(deftest read
  (testing "reading from a reader"
    (is (= {:foo/bar "baz"} (json/read (java.io.StringReader. example))))))

(deftest read-str
  (testing "reading from string"
    (is (= {:foo/bar "baz"} (json/read-str example)))))

(deftest write-str
  (testing "writing to a string"
    (is (= example (json/write-str {:foo/bar "baz"})))))
