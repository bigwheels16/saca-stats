(ns com.jkbff.ps2.real_time_stats.core
    (:require [gniazdo.core :as ws]
              [com.jkbff.ps2.real_time_stats.helper :as helper]
              [com.jkbff.ps2.real_time_stats.config :as config]
              [com.jkbff.ps2.real_time_stats.api :as api]
              [com.jkbff.ps2.real_time_stats.summary :as summary]
              [com.jkbff.ps2.real_time_stats.discord :as discord]
              [clojure.spec.alpha :as s]
              [clojure.spec.test.alpha :as stest]))

(def char-exp (atom {}))

(defn update-experience
    [char-exp-map payload]

    (let [character-id  (:character-id payload)
          experience-id (:experience-id payload)
          amount        (Integer/parseInt (:amount payload))
          current-val   (or (get-in char-exp-map [character-id :experience-events]) (list))
          new-val       (cons {:experience-id experience-id :amount amount} current-val)]

        (if (config/IS_DEV)
            (prn payload))

        (assoc-in char-exp-map [character-id :experience-events] new-val)))

(defn handle-login
    [payload]
    (let [character-id (:character-id payload)
          char-name    (get-in (api/get-characters) [character-id :name :first])
          t            (System/currentTimeMillis)]
        (swap! char-exp #(assoc %1 character-id {:logon t}))
        (helper/log (str "tracking character " char-name " (" character-id ")"))))

(defn append-value
    [m ks v]
    (update-in m ks #(cons v %1)))
(s/fdef append-value
        :args (s/cat :m map? :ks sequential? :v any?)
        :ret map?)

(defn handle-death
    [payload]
    (let [character-id          (:character-id payload)
          attacker-character-id (:attacker-character-id payload)]

        (swap! char-exp #(append-value %1 [character-id :deaths] payload))

        (if (not= character-id attacker-character-id)
            (swap! char-exp #(append-value %1 [attacker-character-id :kills] payload)))))

(defn handle-vehicle
    [payload]
    (let [character-id          (:character-id payload)
          attacker-character-id (:attacker-character-id payload)]

        (swap! char-exp #(append-value %1 [character-id :vehicle-deaths] payload))

        (if (not= character-id attacker-character-id)
            (swap! char-exp #(append-value %1 [attacker-character-id :vehicle-kills] payload)))))

(defn handle-continent-lock
    [payload]
    (let [continent-id   (str (:zone-id payload))
          world-id       (str (:world-id payload))
          continent-name (get-in (api/get-continents) [continent-id :name])
          world-name     (get-in (api/get-worlds) [world-id :name])
          message        (str continent-name " has locked on " world-name "!")]

        (if continent-name
            (discord/send-message message message (list)))))

(defn handle-continent-unlock
    [payload]
    (let [continent-id   (str (:zone-id payload))
          world-id       (str (:world-id payload))
          continent-name (get-in (api/get-continents) [continent-id :name])
          world-name     (get-in (api/get-worlds) [world-id :name])
          message        (str continent-name " has unlocked on " world-name "!")]

        (if continent-name
            (discord/send-message message message (list)))))

(defn handle-message
    [msg]
    (try
        (let [obj     (helper/read-json msg)
              payload (:payload obj)]

            ;(if (not= "heartbeat" (:type obj))
            ;    (helper/log obj))

            (case (:event-name payload)
                "GainExperience" (swap! char-exp update-experience payload)
                "PlayerLogin" (handle-login payload)
                "PlayerLogout" (summary/print-stats payload char-exp)
                "Death" (handle-death payload)
                "VehicleDestroy" (handle-vehicle payload)
                "ContinentLock" (handle-continent-lock payload)
                "ContinentUnlock" (handle-continent-unlock payload)
                nil))
        (catch Exception e (.printStackTrace e))))

(defn handle-close
    [status-code reason]
    (helper/log "Connection closed:" status-code reason))

(defn connect
    []
    (let [client1 (ws/connect (str "wss://push.planetside2.com/streaming?environment=ps2&service-id=s:" (config/SERVICE_ID))
                              :on-receive handle-message
                              :on-close handle-close)

          client2 (ws/connect (str "wss://push.planetside2.com/streaming?environment=ps2&service-id=s:" (config/SERVICE_ID))
                              :on-receive handle-message
                              :on-close handle-close)]

        (ws/send-msg client1 (helper/write-json {:service    "event"
                                                 :action     "subscribe"
                                                 :characters (keys (api/get-characters))
                                                 :eventNames ["GainExperience" "PlayerLogin" "PlayerLogout" "Death" "VehicleDestroy"]}))

        (ws/send-msg client2 (helper/write-json {:service    "event"
                                                 :action     "subscribe"
                                                 :worlds     ["1"]
                                                 :eventNames ["ContinentLock" "ContinentUnlock" "MetagameEvent"]}))

        [client1, client2]))

(defn get-untracked-chars
    []
    (let [char-names-lower (map #(clojure.string/trim (clojure.string/lower-case %)) (config/SUBSCRIBE_CHARACTERS))
          char-names-set   (into #{} char-names-lower)
          api-chars        (into #{} (map #(get-in % [:name :first-lower]) (vals (api/get-characters))))
          diff             (clojure.set/difference char-names-set api-chars)]
        diff))

(defn -main
    [& args]

    (if (config/IS_DEV)
        (stest/instrument))

    (let [is-running      true
          clients         (connect)
          startup-msg     "SACA Stats has started! (v7)"
          untracked-chars (get-untracked-chars)]

        (if (not (config/IS_DEV))
            (if (not (empty? untracked-chars))
                (let [error-msg (str "Untracked chars: " (clojure.string/join "," untracked-chars))]
                    (helper/log error-msg)
                    (discord/send-message startup-msg error-msg []))
                (discord/send-message startup-msg "No errors." [])))

        (while is-running
            (Thread/sleep 1000))))
