(ns boomerang.http-test
  (:require [boomerang.http :as http]
            [clojure.test :refer [deftest is testing]]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.string :as str]))

(stest/instrument)

(deftest parsing-media-types
  (is (= "application/json" (http/parse-media-type "application/json")))
  (is (= "application/json" (http/parse-media-type "application/json; charset=utf-8"))))

(deftest unsupported-media-type
  (is (nil? (http/unsupported-media-type {:headers {"content-type" "application/json"}})))
  (is (= {:status 415
          :headers {"Accepts" "application/json, application/transit+msgpack, application/transit+json, application/edn"}}
       (http/unsupported-media-type {:headers {"content-type" "application/foo"}}))))

(deftest not-acceptable
  (is (nil? (http/not-acceptable {:headers {"accept" "application/json"}})))
  (is (= {:status 406
          :headers {"Consumes" "application/json, application/transit+msgpack, application/transit+json, application/edn"}}
       (http/not-acceptable {:headers {"accept" "application/foo"}}))))

(deftest parsed-body
  (is (= {:foo 1}
         (http/parsed-body {:headers {"content-type" "application/json"}
                            :body (java.io.ByteArrayInputStream. (.getBytes "{\"foo\":1}"))}))))

(deftest body-response
  (let [response (http/body-response 200 {:headers {"accept" "application/json"}} {:foo 1})]
    (is (= 200 (:status response)))
    (is (= {"Content-Type" "application/json"} (:headers response)))
    (is (= "{\"foo\":1}" (slurp (:body response))))))

(s/def :test/number integer?)
(s/def :test/map (s/keys :un-req [:test/number]))

(deftest with-body
  (let [request {:headers {"content-type" "application/json"}
                 :body (java.io.ByteArrayInputStream. (.getBytes "{\"number\":1}"))}
        response (http/with-body [body :test/map request]
                   (http/body-response 200 request body))]
    (is (= {:status 200
            :headers {"Content-Type" "application/json"}
            :body "{\"number\":1}"}
           (update response :body slurp)))))
