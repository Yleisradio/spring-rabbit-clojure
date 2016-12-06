# spring-rabbit-clojure

A simple wrapper for Spring AMQP Rabbit. Hides the complexity of the Spring setup.

[![Clojars Project](https://img.shields.io/clojars/v/spring-rabbit-clojure.svg)](https://clojars.org/spring-rabbit-clojure)

## Usage

Add dependecy: \[spring-rabbit-clojure "0.1.0"]

## Examples

#### Require

```clojure
(:require [spring-rabbit-clojure.core :as rabbit])
```

#### Startup

```clojure
; This must be called before anything else
(rabbit/startup! {:hosts "localhost"
                  :port 5762
                  :vhost "/"
                  :username "guest"
                  :password "guest"
                  :publisher-confirms false
                  :request-heartbeat 10})
```

#### Shutdown

```clojure
(rabbit/stop!)
```

#### Publish

```clojure
(rabbit/publish! exchange routing-key payload headers-map)
```

#### Consume

```clojure
(defn my-handler-fn [headers msg]
  ; headers is a clojure map
  ; msg is the message's payload parsed (see below)
  ; return true to ack the message or false to nack
  true)
  
; Consume the message with my-handler-fn - msg is Message payload parsed to string
(rabbit/consume! queue-name my-handler-fn)

; Consume the message with my-handler-fn - msg is Message payload parsed from json to clojure map
(rabbit/consume! queue-name (json-handler my-handler-fn))

; Consume all messages from a queue (mainly for testing purposes)
; Returns a seq of messages where each message is a seq [headers payload] 
(rabbit/consume-queued! queue-name)
; or
(rabbit/consume-queued! queue-name read-timeout-ms)
```

#### Declaring queues, exchanges and bindings
```clojure
(rabbit/declare-queue! queue {:exclusive false :auto-delete false :durable true})

(rabbit/declare-exchange! exchange-name "topic" {:auto-delete false :durable true})
; or
(rabbit/declare-exchange! exchange-name "direct" {:auto-delete false :durable true})

(rabbit/declare-binding! queue-name exchange-name routing-key)
```

#### Purge queue
```clojure
(rabbit/purge! queue-name)
```

## License

MIT
