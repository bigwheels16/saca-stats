(ns com.jkbff.ps2.real_time_stats.core
    (:require [gniazdo.core :as ws]
              [com.jkbff.ps2.real_time_stats.helper :as helper]
              [com.jkbff.ps2.real_time_stats.config :as config]
              [clj-http.client :as client]))

(def char-exp (atom {}))

(def white-color (* 256 256 256))

(def get-experience-types
    (memoize (fn [] (let [result (client/get (str "https://census.daybreakgames.com/s:" (config/SERVICE_ID) "/get/ps2/experience?c:limit=2000"))
                          body   (helper/read-json (:body result))
                          m      (reduce #(assoc %1 (:experience-id %2) %2) {} (:experience-list body))]
                        m))))

(def get-characters
    (memoize (fn []
                 (let [char-names-lower (map #(clojure.string/trim (clojure.string/lower-case %)) (config/SUBSCRIBE_CHARACTERS))
                       char-names-str   (clojure.string/join "," char-names-lower)
                       url              (str "http://census.daybreakgames.com/s:" (config/SERVICE_ID) "/get/ps2/character?name.first_lower=" char-names-str "&c:limit=" (count char-names-lower))
                       result           (client/get url)
                       body             (helper/read-json (:body result))]
                     (:character-list body)))))

(def get-vehicles
    (memoize (fn []
                 (let [url          (str "http://census.daybreakgames.com/s:" (config/SERVICE_ID) "/get/ps2:v2/vehicle?c:limit=500&c:lang=en")
                       result       (client/get url)
                       body         (helper/read-json (:body result))
                       vehicle-list (:vehicle-list body)]
                     (zipmap (map :vehicle-id vehicle-list) vehicle-list)))))

(defn get-name-by-char-id
    [char-id]
    (let [char-names (get-characters)
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
    [char-name character-id description fields]
    (let [obj  {:embeds [{:title       (str char-name " Stats Summary (" character-id ")")
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
        (str "Time: " (if total-time (str (quot total-time 1000) " secs") "Unknown")
             "\nTotal XP: " total-xp
             "\nXP / min: " xp-per-min
             "\nKills: " num-kills
             "\nDeaths: " num-deaths
             "\nK/D: " kd)))

(defn get-vehicle-stats
    [char-info]
    (let [vehicles-destroyed (:vehicle-kills char-info)
          grouped            (group-by :vehicle-id vehicles-destroyed)
          vehicle-map        (get-vehicles)
          mapped             (map (fn [[k v]] {:vehicle-id k :name (get-in vehicle-map [k :name :en]) :amount (count v)}) grouped)]
        (reverse (sort-by :amount mapped))))

(defn print-stats
    [payload]

    (let [character-id              (:character-id payload)
          char-name                 (get-name-by-char-id character-id)
          char-info                 (get @char-exp character-id)
          most-exp-first            (take 10 (get-char-stats-sorted (:experience-events char-info)))
          exp-list                  (get-experience-types)
          exp-descriptions-added    (map #(assoc % :description (get-in exp-list [(:experience-id %) :description])) most-exp-first)
          summary                   (get-overall-summary char-info)
          xp-summary                (clojure.string/join "\n" (map format-exp-total exp-descriptions-added))
          vehicle-destroyed-summary (clojure.string/join "\n" (map #(str "x" (:amount %1) " - " (:name %1)) (get-vehicle-stats char-info)))
          fields                    [{:name "XP (Top 10)" :value xp-summary}
                                     {:name "Vehicles Destroyed" :value vehicle-destroyed-summary}]]

        (println "sending summary for" char-name "(" character-id ")")
        (send-message-to-discord char-name character-id summary fields)))

(defn handle-login
    [payload]
    (let [character-id (:character-id payload)
          char-name    (get-name-by-char-id character-id)
          t            (System/currentTimeMillis)]
        (swap! char-exp #(assoc %1 character-id {:logon t}))
        (println "tracking character" char-name "(" character-id ")")))

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

(defn handle-message
    [msg]
    (try
        (let [obj     (helper/read-json msg)
              payload (:payload obj)]

            ;(if (not= "heartbeat" (:type obj))
            ;    (println obj))

            (case (:event-name payload)
                "GainExperience" (swap! char-exp update-experience payload)
                "PlayerLogin" (handle-login payload)
                "PlayerLogout" (print-stats payload)
                "Death" (handle-death payload)
                "VehicleDestroy" (handle-vehicle payload)
                nil))
        (catch Exception e (.printStackTrace e))))

(defn handle-close
    [status-code reason]
    (println "Connection closed:" status-code reason))

(defn connect
    []
    (->
        (ws/connect (str "wss://push.planetside2.com/streaming?environment=ps2&service-id=s:" (config/SERVICE_ID))
                    :on-receive handle-message
                    :on-close handle-close)

        (ws/send-msg (helper/write-json {:service    "event"
                                         :action     "subscribe"
                                         :characters (map :character-id (get-characters))
                                         :eventNames (config/SUBSCRIBE_EVENTS)}))))

(defn -main
    [& args]

    (let [is-running true
          web-socket (connect)]

        (while is-running
            (Thread/sleep 1000))))
