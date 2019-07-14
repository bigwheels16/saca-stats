(ns com.jkbff.ps2.real_time_stats.core
    (:require [gniazdo.core :as ws]
              [com.jkbff.ps2.real_time_stats.helper :as helper]
              [com.jkbff.ps2.real_time_stats.config :as config]
              [com.jkbff.ps2.real_time_stats.api :as api]
              [clj-http.client :as client]))

(def char-exp (atom {}))

(def white-color (* 256 256 256))

(def time-units [{:unit "hr" :amount 3600} {:unit "min" :amount 60} {:unit "sec" :amount 1}])

(defn get-time-str
    [timestamp]
    (loop [units-left time-units
           units-arr  []
           time-left  timestamp]

        (if (zero? time-left)
            (clojure.string/join " " units-arr)

            (let [next-unit (first units-left)
                  amount    (quot time-left (:amount next-unit))
                  remainder (rem time-left (:amount next-unit))]

                (if (zero? amount)
                    (recur (rest units-left) units-arr time-left)
                    (recur (rest units-left) (conj units-arr (str amount " " (:unit next-unit))) remainder))))))

(defn get-name-by-char-id
    [char-id]
    (let [char-names (api/get-characters)
          result     (first (filter #(= char-id (:character-id %)) char-names))]
        (get-in result [:name :first])))

(defn update-experience
    [char-exp-map payload]

    (let [character-id  (:character-id payload)
          experience-id (:experience-id payload)
          amount        (Integer/parseInt (:amount payload))
          current-val   (or (get-in char-exp-map [character-id :experience-events]) (list))
          new-val       (cons {:experience-id experience-id :amount amount} current-val)]

        (assoc-in char-exp-map [character-id :experience-events] new-val)))

(defn process-char-info
    [m experience-events]

    (let [{experience-id :experience-id amount :amount} experience-events]
        (update m experience-id (fn [{current-amount :amount current-count :count}]
                                    {:amount (+ (or current-amount 0) amount) :count (inc (or current-count 0)) :experience-id experience-id}))))

(defn get-color-code
    []
    (rand-int (inc white-color)))

(defn get-char-stats-sorted
    [experience-events]

    (let [processed (reduce process-char-info {} experience-events)
          coll      (vals processed)]

        (reverse (sort-by :amount coll))))

(defn send-message-to-discord
    [title description fields]
    (let [obj  {:embeds [{:title       title
                          :type        "rich"
                          :description description
                          :color       (get-color-code)
                          :fields      (filter #(not (clojure.string/blank? (:value %))) fields)}]}
          json (helper/write-json obj)]

        (client/post (config/DISCORD_WEBHOOK_URL) {:body    json
                                                   :headers {"Content-Type" "application/json"}})))

(defn format-exp-total
    [exp-total]
    (str (:amount exp-total) " (x" (:count exp-total) ") - " (or (:description exp-total) (:experience-id exp-total))))

(defn get-total-xp
    [experience-events]
    (reduce #(+ %1 (:amount %2)) 0 experience-events))

(defn get-total-time
    [logon-time]
    (if logon-time
        (- (System/currentTimeMillis) logon-time)))

(defn get-overall-summary
    [char-info]
    (let [total-time (get-total-time (:logon char-info))
          total-xp   (get-total-xp (:experience-events char-info))
          xp-per-min (if total-time (quot (* total-xp 60 1000) total-time) "Unknown")
          num-kills  (count (:kills char-info))
          num-deaths (count (:deaths char-info))
          kd         (float (/ num-kills (if (zero? num-deaths) 1 num-deaths)))]
        (str "Time: " (if total-time (get-time-str (quot total-time 1000)) "Unknown")
             "\nTotal XP: " total-xp
             "\nXP / min: " xp-per-min
             "\nKills: " num-kills
             "\nDeaths: " num-deaths
             "\nK/D: " kd)))

(defn get-vehicle-stats
    [char-info]
    (let [vehicles-destroyed (:vehicle-kills char-info)
          grouped            (group-by :vehicle-id vehicles-destroyed)
          vehicle-map        (api/get-vehicles)
          mapped             (map (fn [[k v]] {:vehicle-id k :name (get-in vehicle-map [k :name :en]) :amount (count v)}) grouped)]
        (reverse (sort-by :amount mapped))))

(defn print-stats
    [payload]

    (let [character-id              (:character-id payload)
          char-name                 (get-name-by-char-id character-id)
          title                     (str char-name " Stats Summary (" character-id ")")
          char-info                 (get @char-exp character-id)
          most-exp-first            (take 10 (get-char-stats-sorted (:experience-events char-info)))
          exp-list                  (api/get-experience-types)
          exp-descriptions-added    (map #(assoc % :description (get-in exp-list [(:experience-id %) :description])) most-exp-first)
          summary                   (get-overall-summary char-info)
          xp-summary                (clojure.string/join "\n" (map format-exp-total exp-descriptions-added))
          vehicle-destroyed-summary (clojure.string/join "\n" (map #(str "x" (:amount %1) " - " (:name %1)) (get-vehicle-stats char-info)))
          fields                    [{:name "XP (Top 10)" :value xp-summary}
                                     {:name "Vehicles Destroyed" :value vehicle-destroyed-summary}]]

        (helper/log "sending summary for" char-name "(" character-id ")")
        (send-message-to-discord title summary fields)))

(defn handle-login
    [payload]
    (let [character-id (:character-id payload)
          char-name    (get-name-by-char-id character-id)
          t            (System/currentTimeMillis)]
        (swap! char-exp #(assoc %1 character-id {:logon t}))
        (helper/log "tracking character" char-name "(" character-id ")")))

(defn append-value
    [m ks v]
    (update-in m ks #(cons v %1)))

(defn handle-death
    [payload]
    (let [character-id          (:character-id payload)
          attacker-character-id (:attacker-character-id payload)]
        (swap! char-exp #(append-value %1 [character-id :deaths] payload))
        (swap! char-exp #(append-value %1 [attacker-character-id :kills] payload))))

(defn handle-vehicle
    [payload]
    (let [character-id          (:character-id payload)
          attacker-character-id (:attacker-character-id payload)]
        (swap! char-exp #(append-value %1 [character-id :vehicle-deaths] payload))
        (swap! char-exp #(append-value %1 [attacker-character-id :vehicle-kills] payload))))

(defn handle-continent-lock
    [payload]
    (let [continents   (api/get-continents)
          continent-id (:zone-id payload)
          continent    (get continents continent-id)
          message      (str (:name continent) " has locked!")]
        (send-message-to-discord message message (list))))

(defn handle-continent-unlock
    [payload]
    (let [continent-id   (:zone-id payload)
          continent-name (get-in (api/get-continents) [continent-id :code])
          message        (str continent-name " has unlocked!")]
        (send-message-to-discord message message (list))))

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
                "PlayerLogout" (print-stats payload)
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
                                                 :characters (map :character-id (api/get-characters))
                                                 :eventNames ["GainExperience" "PlayerLogin" "PlayerLogout" "Death" "VehicleDestroy"]}))

        (ws/send-msg client2 (helper/write-json {:service    "event"
                                                 :action     "subscribe"
                                                 :worlds     ["1"]
                                                 :eventNames ["ContinentLock" "ContinentUnlock" "MetagameEvent"]}))

        [client1, client2]))

(defn -main
    [& args]

    (let [is-running true
          clients    (connect)]

        (while is-running
            (Thread/sleep 1000))))
