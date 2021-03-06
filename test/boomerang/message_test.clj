(ns boomerang.message-test
  (:require [boomerang.message :as message]
            [clojure.test :refer [deftest is testing]]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.spec.test.alpha :as stest]
            [cognitect.transit :as transit]))

(stest/instrument)

(deftest encoding
  (testing "application/edn"
    (is (= "{:foo \"bar\"}" (String. (message/encode
                                      "application/edn"
                                      {:foo "bar"})))))

  (testing "application/transit+json"
    (let [data {:foo "bar"}
          encoded-bytes (message/encode "application/transit+json" data)
          decoded-bytes (transit/read
                         (transit/reader
                          (java.io.ByteArrayInputStream. encoded-bytes) :json))]
      (is (= {:foo "bar"} decoded-bytes))))

  (testing "application/transit+msgpack"
    (let [data {:foo "bar"}
          encoded-bytes (message/encode "application/transit+msgpack" data)
          decoded-bytes (transit/read
                         (transit/reader
                          (java.io.ByteArrayInputStream. encoded-bytes) :msgpack))]
      (is (= {:foo "bar"} decoded-bytes))))

  (testing "application/json"
    (let [data {:foo "bar"}
          encoded-bytes (message/encode "application/json" data)
          decoded-bytes (json/read-str (String. encoded-bytes) :key-fn keyword)]
      (is (= {:foo "bar"} decoded-bytes)))))

(deftest decoding-strings
  (testing "application/edn"
    (is (= {:foo "bar"} (message/decode "application/edn" "{:foo \"bar\"}"))))

  (testing "application/transit+json"
    (let [data {:foo "bar"}
          encoded-string (let [out (java.io.ByteArrayOutputStream.)]
                           (transit/write (transit/writer out :json) data)
                           (.toString out))]
      (is (= {:foo "bar"}
             (message/decode
              "application/transit+json"
              encoded-string)))))

  (testing "application/json"
    (is (= {:foo "bar"} (message/decode "application/json" "{\"foo\": \"bar\"}"))))

  (testing "application/transit+msgpack is unsupported"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"Strings are not supported when using application/transit\+msgpack."
         (message/decode
          "application/transit+msgpack"
          "{}")))))

(deftest decoding-bytes
  (testing "application/edn"
    (is (= {:foo "bar"} (message/decode "application/edn" (.getBytes "{:foo \"bar\"}")))))

  (testing "application/json"
    (is (= {:foo "bar"} (message/decode "application/json" (.getBytes "{\"foo\": \"bar\"}")))))

  (testing "application/transit+json"
    (let [data {:foo "bar"}
          encoded-bytes (let [out (java.io.ByteArrayOutputStream.)]
                           (transit/write (transit/writer out :json) data)
                           (.toByteArray out))]
      (is (= {:foo "bar"}
             (message/decode
              "application/transit+json"
              encoded-bytes)))))

  (testing "application/transit+msgpack"
    (let [data {:foo "bar"}
          encoded-bytes (let [out (java.io.ByteArrayOutputStream.)]
                          (transit/write (transit/writer out :msgpack) data)
                          (.toByteArray out))]
      (is (= {:foo "bar"}
             (message/decode
              "application/transit+msgpack"
              encoded-bytes))))))

(deftest decoding-streams
  (testing "application/edn"
    (is (= {:foo "bar"} (message/decode "application/edn"
                                        (java.io.ByteArrayInputStream. (.getBytes "{:foo \"bar\"}"))))))

  (testing "application/json"
    (is (= {:foo "bar"} (message/decode "application/json"
                                        (java.io.ByteArrayInputStream. (.getBytes "{\"foo\": \"bar\"}"))))))

  (testing "application/transit+json"
    (let [data {:foo "bar"}
          encoded-bytes (let [out (java.io.ByteArrayOutputStream.)]
                           (transit/write (transit/writer out :json) data)
                           (.toByteArray out))]
      (is (= {:foo "bar"}
             (message/decode
              "application/transit+json"
              (java.io.ByteArrayInputStream. encoded-bytes))))))

  (testing "application/transit+msgpack"
    (let [data {:foo "bar"}
          encoded-bytes (let [out (java.io.ByteArrayOutputStream.)]
                          (transit/write (transit/writer out :msgpack) data)
                          (.toByteArray out))]
      (is (= {:foo "bar"}
             (message/decode
              "application/transit+msgpack"
              (java.io.ByteArrayInputStream. encoded-bytes)))))))

(deftest unsupported-args
  (stest/unstrument)

  (testing "decoding application/foo is unsupported"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"Content type \"application/foo\" is not supported."
         (message/decode "application/foo" (.getBytes "{:foo \"bar\"}")))))

  (testing "encoding application/foo is unsupported"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"Content type \"application/foo\" is not supported."
         (message/encode "application/foo" {}))))

  (testing "application/foo is unsupported"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"Body type \"class java.lang.Long\" is not supported."
         (message/decode "application/edn" 1)))))
