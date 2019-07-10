(ns com.jkbff.ps2.real_time_stats.core
    (:require [gniazdo.core :as ws]
              [com.jkbff.ps2.real_time_stats.helper :as helper]
              [com.jkbff.ps2.real_time_stats.config :as config]
              [clj-http.client :as client]))

(def char-exp (atom {}))

(def white-color (* 256 256 256))

(def sample (list
                {:experience-id 278, :amount 150}
                {:experience-id 38, :amount 25}
                {:experience-id 331, :amount 100}
                {:experience-id 331, :amount 100}
                {:experience-id 64, :amount 400}
                {:experience-id 109, :amount 240}
                {:experience-id 111, :amount 64}
                {:experience-id 109, :amount 244}
                {:experience-id 109, :amount 240}
                {:experience-id 331, :amount 100}
                {:experience-id 332, :amount 100}
                {:experience-id 111, :amount 64}
                {:experience-id 252, :amount 20}
                {:experience-id 66, :amount 400}))

(def sample2 (list {:experience-id 279, :amount 300} {:experience-id 37, :amount 10} {:experience-id 373, :amount 50} {:experience-id 389, :amount 25} {:experience-id 1, :amount 100} {:experience-id 373, :amount 50} {:experience-id 389, :amount 25} {:experience-id 373, :amount 50} {:experience-id 373, :amount 50} {:experience-id 389, :amount 25} {:experience-id 389, :amount 25} {:experience-id 335, :amount 25} {:experience-id 278, :amount 150} {:experience-id 29, :amount 200} {:experience-id 99, :amount 6} {:experience-id 99, :amount 6} {:experience-id 99, :amount 6} {:experience-id 291, :amount 250} {:experience-id 291, :amount 250} {:experience-id 99, :amount 6} {:experience-id 99, :amount 6} {:experience-id 2, :amount 48} {:experience-id 2, :amount 55} {:experience-id 2, :amount 50} {:experience-id 103, :amount 52} {:experience-id 1, :amount 100} {:experience-id 107, :amount 120} {:experience-id 2, :amount 85} {:experience-id 8, :amount 10} {:experience-id 1, :amount 100} {:experience-id 278, :amount 150} {:experience-id 8, :amount 10} {:experience-id 8, :amount 10} {:experience-id 1, :amount 100} {:experience-id 54, :amount 30} {:experience-id 1, :amount 100} {:experience-id 38, :amount 25} {:experience-id 1, :amount 100} {:experience-id 303, :amount 6} {:experience-id 109, :amount 68} {:experience-id 303, :amount 6} {:experience-id 303, :amount 6} {:experience-id 303, :amount 6} {:experience-id 303, :amount 6} {:experience-id 303, :amount 6} {:experience-id 2, :amount 56} {:experience-id 389, :amount 25} {:experience-id 38, :amount 25} {:experience-id 8, :amount 10} {:experience-id 1, :amount 100} {:experience-id 8, :amount 10} {:experience-id 1, :amount 100} {:experience-id 10, :amount 25} {:experience-id 8, :amount 10} {:experience-id 1, :amount 100} {:experience-id 2, :amount 100} {:experience-id 2, :amount 40} {:experience-id 8, :amount 10} {:experience-id 1, :amount 100} {:experience-id 8, :amount 10} {:experience-id 1, :amount 100} {:experience-id 2, :amount 28} {:experience-id 291, :amount 250} {:experience-id 37, :amount 10} {:experience-id 10, :amount 25} {:experience-id 8, :amount 10} {:experience-id 1, :amount 100} {:experience-id 2, :amount 48} {:experience-id 54, :amount 30} {:experience-id 8, :amount 10} {:experience-id 1, :amount 100} {:experience-id 389, :amount 25} {:experience-id 398, :amount 20} {:experience-id 109, :amount 168} {:experience-id 286, :amount 20} {:experience-id 302, :amount 6} {:experience-id 302, :amount 6} {:experience-id 302, :amount 6} {:experience-id 302, :amount 6} {:experience-id 302, :amount 6} {:experience-id 302, :amount 6} {:experience-id 302, :amount 6} {:experience-id 291, :amount 250} {:experience-id 291, :amount 250} {:experience-id 302, :amount 5} {:experience-id 302, :amount 6} {:experience-id 302, :amount 6} {:experience-id 302, :amount 3} {:experience-id 8, :amount 10} {:experience-id 1, :amount 100} {:experience-id 8, :amount 10} {:experience-id 1, :amount 100} {:experience-id 54, :amount 30} {:experience-id 8, :amount 10} {:experience-id 1, :amount 100} {:experience-id 385, :amount 200} {:experience-id 8, :amount 10} {:experience-id 1, :amount 100} {:experience-id 54, :amount 30} {:experience-id 10, :amount 25} {:experience-id 8, :amount 10} {:experience-id 1, :amount 100} {:experience-id 2, :amount 34} {:experience-id 1, :amount 100} {:experience-id 278, :amount 150} {:experience-id 303, :amount 6} {:experience-id 303, :amount 6} {:experience-id 303, :amount 6} {:experience-id 302, :amount 6} {:experience-id 302, :amount 6} {:experience-id 302, :amount 6} {:experience-id 302, :amount 6} {:experience-id 302, :amount 6} {:experience-id 302, :amount 6} {:experience-id 302, :amount 6} {:experience-id 302, :amount 6} {:experience-id 291, :amount 250} {:experience-id 291, :amount 750} {:experience-id 302, :amount 6} {:experience-id 302, :amount 5} {:experience-id 2, :amount 44} {:experience-id 303, :amount 6} {:experience-id 303, :amount 6} {:experience-id 303, :amount 6} {:experience-id 303, :amount 6} {:experience-id 302, :amount 6} {:experience-id 373, :amount 50} {:experience-id 278, :amount 150} {:experience-id 8, :amount 10} {:experience-id 291, :amount 2} {:experience-id 291, :amount 750} {:experience-id 10, :amount 25} {:experience-id 8, :amount 10} {:experience-id 1, :amount 100} {:experience-id 37, :amount 10} {:experience-id 8, :amount 10} {:experience-id 1, :amount 100} {:experience-id 279, :amount 300} {:experience-id 8, :amount 10} {:experience-id 336, :amount 0} {:experience-id 335, :amount 25} {:experience-id 8, :amount 10} {:experience-id 1, :amount 100} {:experience-id 37, :amount 10} {:experience-id 8, :amount 10} {:experience-id 37, :amount 10} {:experience-id 1, :amount 100} {:experience-id 372, :amount 186} {:experience-id 303, :amount 6} {:experience-id 303, :amount 6} {:experience-id 2, :amount 31} {:experience-id 1, :amount 100} {:experience-id 302, :amount 13} {:experience-id 110, :amount 105} {:experience-id 278, :amount 150} {:experience-id 8, :amount 10} {:experience-id 1, :amount 100} {:experience-id 2, :amount 48} {:experience-id 62, :amount 400} {:experience-id 38, :amount 25} {:experience-id 1, :amount 100} {:experience-id 304, :amount 102} {:experience-id 291, :amount 750} {:experience-id 107, :amount 120} {:experience-id 304, :amount 102} {:experience-id 304, :amount 102} {:experience-id 58, :amount 100} {:experience-id 2, :amount 41} {:experience-id 1, :amount 100} {:experience-id 1, :amount 100} {:experience-id 279, :amount 300} {:experience-id 291, :amount 750} {:experience-id 651, :amount 500}))

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
    [char-name character-id summary xp-summary]
    (let [obj  {:embeds [{:title       (str char-name " Stats Summary (" character-id ")")
                          :type        "rich"
                          :description summary
                          :color       (get-color-code)
                          :fields      [{:name "XP" :value (if (empty? xp-summary) "No XP" xp-summary)}]}]}
          json (helper/write-json obj)]

        (println json)
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
          total-xp (get-total-xp (:experience-events char-info))
          xp-per-min (if total-time (quot (* total-xp 60 1000) total-time) "Unknown")
          num-kills (count (:kills char-info))
          num-deaths (count (:deaths char-info))
          kd (float (/ num-kills (if (zero? num-deaths) 1 num-deaths)))]
        (str "Time: " (if total-time (str (quot total-time 1000) " secs") "Unknown")
             "\nTotal XP: " total-xp
             "\nXP / min: " xp-per-min
             "\nKills: " num-kills
             "\nDeaths: " num-deaths
             "\nK/D: " kd)))

(defn print-stats
    [payload]

    (let [character-id           (:character-id payload)
          char-name              (get-name-by-char-id character-id)
          char-info              (get @char-exp character-id)
          most-exp-first         (take 10 (get-char-stats-sorted (:experience-events char-info)))
          exp-list               (get-experience-types)
          exp-descriptions-added (map #(assoc % :description (get-in exp-list [(:experience-id %) :description])) most-exp-first)
          summary                (get-overall-summary char-info)
          xp-summary             (clojure.string/join "\n" (map format-exp-total exp-descriptions-added))]

        (println "sending summary for" char-name "(" character-id ")")
        (send-message-to-discord char-name character-id summary xp-summary)))

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
    (let [character-id (:character-id payload)
          attacker-character-id    (:attacker-character-id payload)]
        (swap! char-exp #(append-value %1 [character-id :deaths] payload))
        (swap! char-exp #(append-value %1 [attacker-character-id :kills] payload))))

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
