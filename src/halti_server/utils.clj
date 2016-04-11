(ns halti-server.utils
  (:require [clojure.data.json :as json]
            [clj-time.core :as t]
            [clj-time.coerce :as c]))



(defn json-request [status body]
  {:status  status
   :headers {"Content-Type" "application/json"}
   :body    (json/write-str body)})


(defn uuid [] (str (java.util.UUID/randomUUID)))


(extend-type org.joda.time.DateTime
  json/JSONWriter
  (-write [date out]
    (json/-write (c/to-string date) out)))
