(ns halti-server.service-scheduler
  (:require [halti-server.db :refer [mdb collection-names]]
            [monger.collection :as mc]
            [monger.result :refer [updated-existing?]]
            [clj-time.core :as t]
            [halti-server.events :as events]
            [taoensso.timbre :as timbre :refer [info error warn debug]]
            [halti-server.scheduler]
            [halti-server.solver]
            [halti-server.utils :refer [deadline]]))

(def MB (* 1024 1024))


(defn find-hosts [& args]
  (apply (partial mc/find-maps @mdb (:instances collection-names)) args))

(defn find-services [& args]
  (apply (partial mc/find-maps @mdb (:services collection-names)) args))

(defn update-host [& args]
  (updated-existing? (apply (partial mc/update @mdb (:instances collection-names)) args)))


(defn basic-info+running-services [host]
  "From host.containers get services which are runned in host"
  (assoc host :services #{}))

(defn host->basic-info [host]
  (let [cpu (get-in host [:system :cpus])
        instance-id (:instance_id host)
        containers (get-in host [:config :containers])
        memory (int (/ (get-in host [:info :MemTotal]) MB))]
    {:instance-id instance-id
     :cpu cpu
     :memory memory
     :containers []})) ; Replace with real container config when algorithm supports!

(defn healthy-hosts []
  (let [hosts (find-hosts {:last_heartbeat {"$gt" (deadline)}})]
    (mapv (comp basic-info+running-services host->basic-info) hosts)))

(defn service->basic-service [service]
  {:service-id (:service_id service)
   :memory (:memory service)
   :cpu (:cpu service)
   :instances (:instances service)})

(defn enabled-services []
  (let [services (find-services {:enabled true})]
    (mapv service->basic-service services)))

;{:name "App A" :cpu 0.8  :memory 500 :instances 3}

(defn save-host-containers! [host]
  (let [instance-id (:instance-id host)
        containers (:containers host)]
    ;(info "CONTAINERS FOR MACHINE" instance-id containers)))
    (update-host {:instance_id instance-id} {"$set" {:config {:containers containers}}})))

(defn save-container-error! [container]
  (warn "Container didn't fit into current cluster" container))


(defn save-container-distribution! [hosts]
  (doall (map save-host-containers! hosts)))


(defn save-container-distribution-errors! [containers]
  (doall (map save-container-error! containers)))



(defn schedule! [& args]
  (info "Scheduling....")
  (let [hosts (healthy-hosts)
        services (enabled-services)
        distribution (halti-server.solver/distribute-services hosts services)]
    (save-container-distribution! distribution)))
    ;(save-container-distribution-errors! (:left-over-containers distribution))))

(defn start-scheduler! []
  (info "Scheduler started!")
  (events/observe-updates schedule!))
