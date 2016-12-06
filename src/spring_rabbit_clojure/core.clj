(ns spring-rabbit-clojure.core
  (:require [clojure.tools.logging :refer [debug info]]
            [cheshire.core :as json]
            [clojure.walk :refer [keywordize-keys]])
  (:import (org.springframework.amqp.rabbit.core RabbitTemplate ChannelAwareMessageListener RabbitAdmin)
           (org.springframework.amqp.rabbit.connection CachingConnectionFactory)
           (org.springframework.amqp.rabbit.listener SimpleMessageListenerContainer)
           (org.springframework.amqp.core AcknowledgeMode Message MessageProperties Queue DirectExchange TopicExchange Binding Binding$DestinationType)))


(def connection (atom nil))
(def rabbit (atom nil))
(def admin (atom nil))
(def consumers (atom ()))

(defn startup! [{:keys [hosts port vhost username password] :as opts}]
  (let [cf (doto (CachingConnectionFactory. hosts port)
             (.setVirtualHost vhost)
             (.setUsername username)
             (.setPassword password))
        rabbit-template (RabbitTemplate. cf)
        rabbit-admin (RabbitAdmin. cf)]
    (swap! connection (fn [x] cf))
    (swap! rabbit (fn [x] rabbit-template))
    (swap! admin (fn [x] rabbit-admin))))

(defn publish! [exchange routing-key payload-map headers-map]
  (let [body-bytes (-> payload-map
                       (json/generate-string)
                       (.getBytes))
        message-properties (MessageProperties.)
        _ (doseq [[k v] headers-map]
            (.setHeader message-properties k v))
        message (Message. body-bytes message-properties)]
    (.send @rabbit exchange routing-key message)))

(defn consumer [handler-fn]
  (fn [msg channel]
    (let [delivery-tag (-> msg .getMessageProperties .getDeliveryTag)
          headers (->> msg
                       .getMessageProperties
                       .getHeaders
                       (into {})
                       keywordize-keys)
          body (String. (.getBody msg))]
      (if (handler-fn headers body)
        (.basicAck channel delivery-tag false)
        (.basicReject channel delivery-tag false)))))

(defn json-handler [handler-fn]
  (fn [headers body]
    (let [msg (json/parse-string body true)]
      (handler-fn headers msg))))

(defn consume! [queue handler-fn]
  (let [listener (reify ChannelAwareMessageListener
                   (onMessage [this msg channel] ((consumer handler-fn) msg channel)))
        container (doto (SimpleMessageListenerContainer. @connection)
                    (.setMessageListener listener)
                    (.setQueueNames (into-array String [queue]))
                    (.setAcknowledgeMode AcknowledgeMode/MANUAL)
                    (.start))]
    (swap! consumers conj container)))

(defn consume-all-from-queue! [queue messages timeout]
  (let [msg (.receive @rabbit queue timeout)]
    (if msg
      (let [headers (->> msg
                         .getMessageProperties
                         .getHeaders
                         (into {})
                         keywordize-keys)
            body (-> msg
                     (.getBody)
                     (String.))]
        (swap! messages conj [headers (json/parse-string body true)])
        (recur queue messages timeout))
      @messages)))

(defn consume-queued!
  ([queue]
   (consume-all-from-queue! queue (atom []) 200))
  ([queue timeout]
   (consume-all-from-queue! queue (atom []) timeout)))

(defn declare-queue! [name {:keys [exclusive auto-delete durable]}]
  (let [queue (Queue. name durable exclusive auto-delete)]
    (.declareQueue @admin queue)))

(defn declare-exchange! [name type {:keys [auto-delete durable]}]
  (let [exchange (case type
                   "topic" (TopicExchange. name durable auto-delete)
                   "direct" (DirectExchange. name durable auto-delete))]
    (.declareExchange @admin exchange)))

(defn declare-binding! [queue exchange routing-key]
  (.declareBinding @admin (Binding. queue Binding$DestinationType/QUEUE exchange routing-key {})))

(defn purge! [queue]
  (.purgeQueue @admin queue true))

(defn stop! []
  (info "Stopping RabbitMQ")
  (doseq [c @consumers]
    (do
      (info "Stopping " c)
      (.stop c)))
  (info "Done with consumers")
  (when @connection
    (do
      (info "Stopping " @connection)
      (.destroy @connection)
      (info "Connection stopped"))))
