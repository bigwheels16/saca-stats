(ns com.jkbff.ps2.real_time_stats.core
    (:require [gniazdo.core :as ws]
              [com.jkbff.ps2.real_time_stats.helper :as helper]
              [com.jkbff.ps2.real_time_stats.config :as config]
              [com.jkbff.ps2.real_time_stats.api :as api]
              [com.jkbff.ps2.real_time_stats.summary :as summary]
              [com.jkbff.ps2.real_time_stats.discord :as discord]
              [clojure.spec.alpha :as s]
              [clojure.spec.test.alpha :as stest]))

(def last-heartbeat (atom (System/currentTimeMillis)))
(def char-exp (atom {}))
(def is-running (atom true))

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
    [payload char-map]
    (let [character-id (:character-id payload)
          char-name    (get-in char-map [character-id :name :first])
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
    [payload char-map]
    (let [character-id          (:character-id payload)
          attacker-character-id (:attacker-character-id payload)]

        (if (contains? char-map character-id)
            (swap! char-exp #(append-value %1 [character-id :deaths] payload)))

        (if (and (contains? char-map attacker-character-id) (not= character-id attacker-character-id))
            (swap! char-exp #(append-value %1 [attacker-character-id :kills] payload)))))

(defn handle-vehicle
    [payload char-map]
    (let [character-id          (:character-id payload)
          attacker-character-id (:attacker-character-id payload)]

        (if (contains? char-map character-id)
            (swap! char-exp #(append-value %1 [character-id :vehicle-deaths] payload)))

        (if (and (contains? char-map attacker-character-id) (not= character-id attacker-character-id))
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

(defn handle-facility-capture
    [payload]
    (let [character-id (:character-id payload)]
        (swap! char-exp #(append-value %1 [character-id :facility-capture] payload))))

(defn handle-facility-defend
    [payload]
    (let [character-id (:character-id payload)]
        (swap! char-exp #(append-value %1 [character-id :facility-defend] payload))))

(defn handle-message
    [char-map msg]
    (try
        (let [obj     (helper/read-json msg)
              payload (:payload obj)]

            (if (= "heartbeat" (:type obj))
                (reset! last-heartbeat (System/currentTimeMillis)))

            ;(if (not= "heartbeat" (:type obj))
            ;    (helper/log obj))

            (case (:event-name payload)
                "GainExperience" (swap! char-exp update-experience payload)
                "PlayerLogin" (handle-login payload char-map)
                "PlayerLogout" (summary/print-stats payload char-exp char-map)
                "Death" (handle-death payload char-map)
                "VehicleDestroy" (handle-vehicle payload char-map)
                "ContinentLock" (handle-continent-lock payload)
                "ContinentUnlock" (handle-continent-unlock payload)
                "PlayerFacilityCapture" (handle-facility-capture payload)
                "PlayerFacilityDefend" (handle-facility-defend payload)
                nil))
        (catch Exception e (.printStackTrace e))))

(defn handle-close
    [status-code reason]
    (reset! is-running false)
    (helper/log "Connection closed:" status-code reason))

(defn connect
    [char-map]
    (let [client1 (ws/connect (str "wss://push.planetside2.com/streaming?environment=ps2&service-id=s:" (config/SERVICE_ID))
                              :on-receive (partial handle-message char-map)
                              :on-close handle-close)

          ;client2 (ws/connect (str "wss://push.planetside2.com/streaming?environment=ps2&service-id=s:" (config/SERVICE_ID))
          ;                    :on-receive handle-message
          ;                    :on-close handle-close)
          ]

        (ws/send-msg client1 (helper/write-json {:service    "event"
                                                 :action     "subscribe"
                                                 :characters (keys char-map)
                                                 :eventNames ["GainExperience" "PlayerLogin" "PlayerLogout" "Death" "VehicleDestroy" "PlayerFacilityCapture" "PlayerFacilityDefend"]}))

        ;(ws/send-msg client2 (helper/write-json {:service    "event"
        ;                                         :action     "subscribe"
        ;                                         :worlds     ["1"]
        ;                                         :eventNames ["ContinentLock" "ContinentUnlock" "MetagameEvent"]}))

        [client1]))

(defn get-untracked-chars
    [char-map]
    (let [char-names-lower (map #(clojure.string/trim (clojure.string/lower-case %)) (config/SUBSCRIBE_CHARACTERS))
          char-names-set   (into #{} char-names-lower)
          api-chars        (into #{} (map #(get-in % [:name :first-lower]) (vals char-map)))
          diff             (clojure.set/difference char-names-set api-chars)]
        diff))

(defn is-connected?
    [max-ms]
    (let [time-since-heartbeat (- (System/currentTimeMillis) @last-heartbeat)]
        (if (> time-since-heartbeat max-ms)
            (do (helper/log (str "No heartbeat for " time-since-heartbeat "ms. Shutting down..."))
                (reset! is-running false)))))

(defn get-outfit-characters
    [outfit-ids]
    (apply concat (map #(:members (api/get-outfit-by-id %)) outfit-ids)))

(defn -main
    [& args]

    (if (config/IS_DEV)
        (stest/instrument))

    (let [characters          (concat (api/get-characters (config/SUBSCRIBE_CHARACTERS)) (get-outfit-characters (config/SUBSCRIBE_OUTFITS)))
          char-map            (zipmap (map :character-id characters) characters)
          clients             (connect char-map)
          startup-msg         (str "SACA Stats (v10) has started! Tracking " (count characters) " characters.")
          untracked-chars     (get-untracked-chars char-map)
          is-connected-future (helper/callback-interval (partial is-connected? 60000) 30000)]

        (if (not (config/IS_DEV))
            (if (not (empty? untracked-chars))
                (let [error-msg (str "Untracked chars: " (clojure.string/join "," untracked-chars))]
                    (helper/log error-msg)
                    (discord/send-message startup-msg error-msg []))
                (discord/send-message startup-msg "No errors." [])))

        (while @is-running
            (Thread/sleep 1000))

        ; cleanup
        (future-cancel is-connected-future)
        (doseq [c clients]
            (ws/close c))

        ; return non-zero exit code to indicate error
        (System/exit 1)))
