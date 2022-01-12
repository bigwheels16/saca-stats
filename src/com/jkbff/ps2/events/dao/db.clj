(ns com.jkbff.ps2.events.dao.db
    (:require [com.jkbff.ps2.real-time-stats.config :as config]
              [hikari-cp.core :as hikari]))

;(defn get-db-pool
;    []
;    (hikari/make-datasource {:adapter        (config/DATABASE_TYPE)
;                             :database-name  (config/DATABASE_NAME)
;                             :server-name    (config/DATABASE_HOST)
;                             :username       (config/DATABASE_USERNAME)
;                             :password       (config/DATABASE_PASSWORD)}))

(defn get-db-pool
    []
    (hikari/make-datasource {:adapter "h2"
                             :url     "jdbc:h2:mem:events"
                             ;:url     "jdbc:h2:./test"
                             }))

(defn extract-single-result
    [result]
    (case (count result)
        0 nil
        1 (first result)
        (throw (Exception. "More than one result when 0 or 1 results expected"))))

(defn require-single-result
    [result]
    (case (count result)
        1 (first result)
        (throw (Exception. "More than one result or no results when 1 result expected"))))
