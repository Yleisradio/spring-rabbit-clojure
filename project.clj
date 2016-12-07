(defproject spring-rabbit-clojure "0.1.0"
  :description "A Simple clojure wrapper for Spring AMQP Rabbit"
  :url "https://github.com/Yleisradio/spring-rabbit-clojure"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.springframework.amqp/spring-rabbit "1.6.5.RELEASE"]
                 [cheshire "5.6.3"]]
  :main ^:skip-aot spring-rabbit-clojure.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
