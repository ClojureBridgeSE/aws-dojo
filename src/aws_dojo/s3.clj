(ns aws-dojo.s3
  (:require [amazonica.aws.s3 :as s3])
  (:import (java.io ByteArrayInputStream)
           (java.time Instant)))

(def ^:dynamic *bucket-prefix* "clojure.aws-dojo.")
(def ^:dynamic *url-expiry-hours* 1)

(defn- ->stream [s]
  (let [bs (.getBytes s)]
    {:input (ByteArrayInputStream. bs)
     :size (count bs)}))

(defn- expiry-ts []
  (-> (Instant/now)
      (.plusSeconds (* 3600 *url-expiry-hours*))
      (.toEpochMilli)))

(defn create-bucket! [name]
  (:name (s3/create-bucket (str *bucket-prefix* name))))

(defn put! [bucket key val]
  (let [{:keys [size input]} (->stream val)]
    (s3/put-object :bucket-name bucket
                   :key key
                   :input-stream input
                   :metadata {:content-length size
                              :content-type "text/html"})
    (str (s3/generate-presigned-url :bucket-name bucket
                                    :key key
                                    :expiration (expiry-ts)
                                    :method "GET"))))
