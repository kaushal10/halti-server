(ns halti-server.api.loadbalancers.extra
  (:require [halti-server.utils :refer [json-request uuid]]
            [halti-server.api.loadbalancers.db :as db :refer [find-loadbalancers]]
            [halti-server.api.instances.db :refer [find-instances]]
            [halti-server.api.services.db :refer [find-services]]
            [clj-time.core :as t]
            [halti-server.events :as events]
            [halti-server.utils :refer [deadline]]))



(defn- hosts->containers [host]
  (map #(assoc % :host-instance-id (:instance_id host)) (:containers host)))

(defn- decorated-port-f [base]
  (fn port->beautiful-port [port]
    (merge base
      {:address (:IP port)
       :port (:PublicPort port)
       :source (:PrivatePort port)})))

(defn- container->service-port-pairs [container]
  (let [ports (:Ports container)
        service-id (subs (first (:Names container)) 1)
        instance-id (:host-instance-id container)
        port->beautiful-port (decorated-port-f {:service_id service-id
                                                :instance_id instance-id})] ;})]
    (map port->beautiful-port ports)))


(defn- service-port-pairs->grouped-addresses [service-port-pairs]
  (group-by :service_id service-port-pairs))


(defn- remove-service-id+source [port]
  (-> port
    (dissoc :service_id)
    (dissoc :source)))


(defn- ports+loadbalancer->loadbalancer-config [ports loadbalancer]
  (let [loadbalancer-source (:source_port loadbalancer)
        only-selected-port (fn [port] (= (:source port) loadbalancer-source))
        service-id (:service_id loadbalancer)
        service-ports (get ports service-id)
        selected-ports (mapv remove-service-id+source (filter only-selected-port service-ports))]
    (merge loadbalancer {:backends selected-ports})))

(defn- loadbalancer-configuration-data []
  (let [enabled-loadbalancers (find-loadbalancers {:enabled true})
        healthy-hosts (find-instances {:last_heartbeat {"$gt" (deadline)}})
        ports-by-services (service-port-pairs->grouped-addresses
                            (mapcat container->service-port-pairs
                              (mapcat hosts->containers healthy-hosts)))
        enabled-services (find-services {:enabled true})
        loadbalancer+backends (partial ports+loadbalancer->loadbalancer-config ports-by-services)]
    (mapv loadbalancer+backends enabled-loadbalancers)))




(defn loadbalancer-configuration [req]
  (json-request 200 (loadbalancer-configuration-data)))