(ns boomerang.json
  (:refer-clojure :exclude [read])
  (:require [clojure.data.json :as json]))

(def ^:private date-formatter java.time.format.DateTimeFormatter/BASIC_ISO_DATE)

(defn ^:private unkeyword [k]
  (cond
    (string? k) k
    (keyword? k) (let [kns (namespace k)
                       kn (name k)]
                   (if kns
                     (str kns "/" kn)
                     kn))
    :else (throw (ex-info (str "Invalid key: " k) {:key k
                                                   :type (type k)}))))

(defn ^:private format-date [k v]
  (if (instance? java.time.LocalDate v)
    (.format v date-formatter)
    v))

(defn read [reader] (json/read reader :key-fn keyword))
(defn read-str [body] (json/read-str body :key-fn keyword))
(defn write-str [body] (json/write-str body :key-fn unkeyword :value-fn format-date))
