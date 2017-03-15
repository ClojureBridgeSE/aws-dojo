(ns aws-dojo.sqs
  (:require [amazonica.aws.sqs :as sqs]
            [cheshire.core :as json]))

(defn create-queue! [name]
  (sqs/create-queue name))

(defn send-message [queue msg]
  (sqs/send-message queue msg))

(defn receive-message [queue]
  (sqs/receive-message :queue-url (:queue-url queue)
                       :delete true))

(defn get-command [message]
  (-> message
      :messages
      first
      :body
      (json/parse-string true)))
