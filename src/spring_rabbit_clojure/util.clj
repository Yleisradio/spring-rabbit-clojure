(ns spring-rabbit-clojure.util
  (:require [clojure.walk :refer [keywordize-keys]]))

(defn headers->map [message]
  (->> message
       .getMessageProperties
       .getHeaders
       (into {})
       keywordize-keys))

(defn body->string [message]
  (-> message
      (.getBody)
      (String.)))
