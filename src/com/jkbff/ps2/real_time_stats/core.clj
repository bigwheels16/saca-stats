(ns com.jkbff.ps2.real-time-stats.core
    (:require [gniazdo.core :as ws]
              [com.jkbff.helper :as helper]
              [com.jkbff.ps2.real-time-stats.config :as config]
              [com.jkbff.ps2.real-time-stats.api :as api]
              [com.jkbff.ps2.real-time-stats.summary :as summary]
              [com.jkbff.ps2.real-time-stats.discord :as discord]
              [com.jkbff.ps2.events.dao.events :as events]
              [clojure.spec.alpha :as s]
              [clojure.spec.test.alpha :as stest]
              [clojure.tools.logging :as log]
              [com.jkbff.ps2.events.dao.db :as db]))

(def last-heartbeat (atom (System/currentTimeMillis)))
(def is-running (atom true))

(defn append-value
    [m ks v]
    (update-in m ks #(cons v %1)))
(s/fdef append-value
        :args (s/cat :m map? :ks sequential? :v any?)
        :ret map?)

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
    [ds msg]
    (try
        (let [obj     (helper/read-json msg)
              payload (:payload obj)]

            (if (not= "heartbeat" (:type obj))
                (log/debug obj))

            ; set last-heartbeat for every payload, not just heartbeat
            (reset! last-heartbeat (System/currentTimeMillis))

            (try
                (case (:event-name payload)
                    "GainExperience" (events/save-experience-event ds payload)
                    "Death" (do
                              (events/save-death-event ds payload)
                              ;(summary/print-stats ds (:character-id payload))
                              )
                    "VehicleDestroy" (events/save-vehicle-destroy-event ds payload)
                    "PlayerLogin" (do
                                    (events/save-player-login-event ds payload)
                                    (log/info (str "tracking character: '" (:character-id payload) "'"))
                                    )
                    "PlayerLogout" (do
                                     (summary/print-stats ds (:character-id payload))
                                     (events/delete-player-events ds (:character-id payload) (:timestamp payload))
                                     )
                    ;"PlayerLogout" (events/save-player-logout-event db-conn payload)
                    "PlayerFacilityCapture" (events/save-facility-capture-event ds payload)
                    "PlayerFacilityDefend" (events/save-facility-defend-event ds payload)
                    "FacilityControl" (events/save-facility-control-event ds payload)
                    "ContinentLock" (handle-continent-lock payload)
                    "ContinentUnlock" (handle-continent-unlock payload)
                    nil)
                (catch Exception e (throw (Exception. (str "error processing event: " obj) e)))))
        (catch Exception e (log/error e))))

(defn handle-close
    [status-code reason]
    (reset! is-running false)
    (log/info (str "Connection closed:" status-code reason)))

(defn connect
    [char-map servers ds]
    (let [client1 (ws/connect (str "wss://push.planetside2.com/streaming?environment=ps2&service-id=s:" (config/SERVICE_ID))
                              :on-receive (partial handle-message ds)
                              :on-close handle-close)]

        (ws/send-msg client1 (helper/write-json {:service    "event"
                                                 :action     "subscribe"
                                                 :characters (or (keys char-map) ["all"])
                                                 :worlds     servers
                                                 :eventNames ["GainExperience" "PlayerLogin" "PlayerLogout" "Death" "VehicleDestroy" "PlayerFacilityCapture" "PlayerFacilityDefend"]
                                                 :logicalAndCharactersWithWorlds (empty? char-map)}))

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
            (do (log/info (str "No heartbeat for " time-since-heartbeat "ms. Shutting down..."))
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
          ds                  (db/get-db-pool)
          clients             (connect char-map (config/SUBSCRIBE_SERVERS) ds)
          version             (clojure.string/trim (slurp "version.txt"))
          startup-msg         (str "SACA Stats (v" version ") has started!\nTracking " (count characters) " characters and " (count (config/SUBSCRIBE_SERVERS)) " servers.")
          untracked-chars     (get-untracked-chars char-map)
          is-connected-future (helper/callback-interval (partial is-connected? 60000) 30000)]

        (events/create-event-tables ds)
        (events/populate-loadout-table (vals (api/get-loadouts)) ds)

        (if (not (empty? untracked-chars))
            (let [error-msg (str "Untracked chars: " (clojure.string/join "," untracked-chars))]
                (log/error error-msg)
                (discord/send-message startup-msg error-msg []))
            (discord/send-message startup-msg "No errors." []))

        (while @is-running
            (Thread/sleep 1000))

        ; cleanup
        (future-cancel is-connected-future)
        (doseq [c clients]
            (ws/close c))

        ; return non-zero exit code to indicate error
        (System/exit 1)))
