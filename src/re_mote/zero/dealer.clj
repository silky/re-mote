(ns re-mote.zero.dealer
  "A client dealer socket"
  (:require
     [clojure.core.strint :refer  (<<)]
     [taoensso.timbre :refer  (refer-timbre)]
     [re-mote.zero.common :refer  (read-key client-socket context close!)])
  (:import
     [org.zeromq ZMsg ZMQ ZMQ$PollItem ZMQ$Poller]
     [java.nio.charset Charset]))

(refer-timbre)

(defn dealer-socket [host parent]
  (let [id (format "%04X-%04X" (rand-int 30) (rand-int 30))]
    (doto (client-socket ZMQ/DEALER parent)
      (.setIdentity (bytes id))
      (.connect (<< "tcp://~{host}:9000")))))

(def sockets (atom {}))

(defn setup-client [host parent]
  (reset! sockets
    {:dealer (dealer-socket host parent)}))

(defn read-loop []
  (let [{:keys [dealer]} @sockets items (object-array [(ZMQ$PollItem. dealer ZMQ$Poller/POLLIN)]) ]
    (while true
       (ZMQ/poll items 10)
       (when (.isReadable (aget items 0))
          (let [msg (.recvStr dealer) ]
            (info msg))))))

(defn send- [s]
  (let [{:keys [dealer]} @sockets]
    (.send dealer s)))