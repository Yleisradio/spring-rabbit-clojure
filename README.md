# spring-rabbit-clojure

A simple wrapper for Spring AMQP Rabbit. Hides the complexity of the Spring setup.

## Installation

    git clone git@github.com:Yleisradio/spring-rabbit-clojure.git.
    
    lein install

## Usage

Add dependecy: \[spring-rabbit-clojure "0.1.0"]

## Examples

#### Startup

```clojure
; This must be called before anything else
(rabbit/startup! {:hosts "localhost"
                  :port 5762
                  :vhost "/"
                  :username "guest"
                  :password "guest
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
```

#### Declaring queues, exchanges and bindings
```clojure
(rabbit/declare-queue! queue {:exclusive false :auto-delete false :durable true})

(rabbit/declare-exchange! exchange-name "topic" {:auto-delete false :durable true})
; or
(rabbit/declare-exchange! exchange-name "direct" {:auto-delete false :durable true})

(rabbit/declare-binding! queue-name exchange-name routing-key)
```


## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
