(ns boomerang.json-test
  (:require [boomerang.json :as json]
            [clojure.test :refer :all]))

(def example "{\"foo\\/bar\":\"baz\"}")

(def date-formatter java.time.format.DateTimeFormatter/BASIC_ISO_DATE)

(deftest reading-from-a-reader
  (testing "reading from a reader"
    (is (= {:foo/bar "baz"} (json/read (java.io.StringReader. example))))))

(deftest reading-from-a-string
  (testing "reading from string"
    (is (= {:foo/bar "baz"} (json/read-str example)))))

(deftest writing-to-a-string
  (testing "writing to a string"
    (is (= example (json/write-str {:foo/bar "baz"})))))

(deftest writing-to-a-string-with-a-date
  (testing "writing to a string with a date"
    (let [date (java.time.LocalDate/parse "20170101" date-formatter)]
      (is (= "{\"foo\\/bar\":\"20170101\"}"
             (json/write-str {:foo/bar date}))))))
