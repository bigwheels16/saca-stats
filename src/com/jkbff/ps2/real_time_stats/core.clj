(ns com.jkbff.ps2.real_time_stats.core
  (:require [gniazdo.core :as ws]
            [com.jkbff.ps2.real_time_stats.helper :as helper]
            [com.jkbff.ps2.real_time_stats.config :as config]))

(defn handle-message
  [msg]
  (let [obj (helper/read-json msg)]
      (if (not= "heartbeat" (:type obj))
          (println obj))))

(defn connect
    []
    (->
        (ws/connect (str "wss://push.planetside2.com/streaming?environment=ps2&service-id=s:" (config/SERVICE_ID))
                    :on-receive handle-message)

        (ws/send-msg (helper/write-json {:service "event"
                                         :action "subscribe"
                                         :characters (config/SUBSCRIBE_CHARACTER_IDS)
                                         :eventNames (config/SUBSCRIBE_EVENTS)}))))

(defn -main
    [& args]

    (let [is-running true
        web-socket (connect)]

      (while is-running
          (Thread/sleep 1000))))
