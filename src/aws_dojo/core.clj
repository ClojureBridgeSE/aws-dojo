(ns aws-dojo.core
  (:require [amazonica.core :as aws]
            [aws-dojo.drawing :as drawing]
            [aws-dojo.s3 :as s3]
            [aws-dojo.sqs :as sqs]
            [environ.core :refer [env]]))

(defn authenticate! []
  (aws/defcredential
    (env :aws-access-key-id) (env :aws-secret-access-key) (env :aws-region)))

(defn get-commands [queue]
  (loop [commands []]
    (if-let [command (sqs/get-command (sqs/receive-message queue))]
      (let [commands (conj commands command)]
        (if (drawing/complete? commands)
          commands
          (recur commands)))
      (recur commands))))

(defn make-drawing [queue]
  (->> queue
       get-commands
       drawing/render))

(defn spit-html [queue filename]
  (->> queue
       make-drawing
       (spit filename)))

(defn run [queue-name file-name bucket-name]
  (let [queue (sqs/create-queue! queue-name)
        bucket (s3/create-bucket! bucket-name)
        drawing (make-drawing queue)]
    (println "Here is your drawing:\n" (s3/put! bucket file-name drawing))))
