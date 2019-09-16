(ns com.jkbff.ps2.real_time_stats.summary
    (:require [com.jkbff.ps2.real_time_stats.api :as api]
              [com.jkbff.ps2.real_time_stats.helper :as helper]
              [com.jkbff.ps2.real_time_stats.discord :as discord]
              [clojure.spec.alpha :as s]))

(s/def ::infantry-count int?)
(s/def ::vehicle-count int?)
(s/def ::item-name string?)
(s/def ::kills seq?)
(s/def ::vehicle-kills string?)

(defn process-char-info
    [m experience-events]
    (let [{experience-id :experience-id amount :amount} experience-events]
        (update m experience-id (fn [{current-amount :amount current-count :count}]
                                    {:amount (+ (or current-amount 0) amount) :count (inc (or current-count 0)) :experience-id experience-id}))))

(defn get-char-stats-sorted
    [experience-events]
    ; TODO use group-by instead?
    (let [processed (reduce process-char-info {} experience-events)
          coll      (vals processed)]
        (reverse (sort-by :amount coll))))

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
(s/fdef get-total-time
        :args (s/cat :login-time number?)
        :ret number?)

(defn get-overall-summary
    [char-info]
    (let [total-time         (get-total-time (:logon char-info))
          total-xp           (get-total-xp (:experience-events char-info))
          xp-per-min         (if total-time (quot (* total-xp 60 1000) total-time) "Unknown")
          num-kills          (count (:kills char-info))
          num-deaths         (count (:deaths char-info))
          kd                 (float (/ num-kills (if (zero? num-deaths) 1 num-deaths)))
          vehicle-map        (api/get-vehicles)
          vehicles-used      (reduce + 0 (map #(get-in vehicle-map [(:vehicle-id %) :cost] 0) (:vehicle-deaths char-info)))
          vehicles-destroyed (reduce + 0 (map #(get-in vehicle-map [(:vehicle-id %) :cost] 0) (:vehicle-kills char-info)))
          nanite-efficiency  (float (/ vehicles-destroyed (if (zero? vehicles-used) 1 vehicles-used)))]
        (str "Time: " (if total-time (helper/get-time-str (quot total-time 1000)) "Unknown")
             "\nTotal XP: `" total-xp "` XP / min: `" xp-per-min "`"
             "\nKills: `" num-kills "` Deaths: `" num-deaths "` K/D: `" kd "`"
             "\nNanites Used: `" vehicles-used "` Nanites Destroyed: `" vehicles-destroyed "` Nanite Efficiency: `" nanite-efficiency "`")))

(defn get-max-kills
    [char-info]
    (let [infantry-kills (:kills char-info)
          grouped        (group-by :loadout-id infantry-kills)
          infantry-map   (api/get-loadouts)
          mapped         (map (fn [[k v]] {:loadout-id k :name (get-in infantry-map [k :code-name]) :amount (count v)}) grouped)
          filtered       (filter #(case (:loadout-id %)
                                      "7" true
                                      "14" true
                                      "21" true
                                      false) mapped)]
        filtered))

(defn get-vehicle-kill-stats
    [char-info]
    (let [vehicles-destroyed (filter #(not= "0" (:attacker-vehicle-id %)) (:vehicle-kills char-info)) ; only count vehicles destroyed from a vehicle
          grouped            (group-by :vehicle-id vehicles-destroyed)
          vehicle-map        (api/get-vehicles)
          mapped             (map (fn [[k v]] {:vehicle-id k :name (get-in vehicle-map [k :name :en]) :amount (count v)}) grouped)
          filtered           (filter #(get-in vehicle-map [(:vehicle-id %) :cost]) mapped)]
        (reverse (sort-by :amount filtered))))

(defn get-vehicle-lost-stats
    [char-info]
    (let [vehicles-lost (:vehicle-deaths char-info)
          grouped       (group-by :vehicle-id vehicles-lost)
          vehicle-map   (api/get-vehicles)
          mapped        (map (fn [[k v]] {:vehicle-id k :name (get-in vehicle-map [k :name :en]) :amount (count v)}) grouped)
          filtered      (filter #(get-in vehicle-map [(:vehicle-id %) :cost]) mapped)]
        (reverse (sort-by :amount filtered))))

(defn get-weapon-name
    [item-id]
    (if (= "0" item-id)
        "RAM"
        (or (get-in (api/get-item-info item-id) [:name :en]) (str "Unknown(" item-id ")"))))

(defn get-kills-by-weapon
    [char-info]
    (let [vehicles-destroyed (:vehicle-kills char-info)
          vehicles-grouped   (group-by :attacker-weapon-id vehicles-destroyed)
          infantry-killed    (:kills char-info)
          infantry-grouped   (group-by :attacker-weapon-id infantry-killed)
          item-ids           (-> #{}
                                 (into (keys vehicles-grouped))
                                 (into (keys infantry-grouped)))
          mapped             (map #(hash-map :item-id %
                                             :item-name (get-weapon-name %)
                                             :vehicle-count (count (get vehicles-grouped % []))
                                             :infantry-count (count (get infantry-grouped % [])))
                                  item-ids)]

        (reverse (sort-by #(+ (:vehicle-count %) (:infantry-count %)) mapped))))
(s/fdef get-kills-by-weapon
        :args (s/cat :row (s/keys :req [::vehicle-kills ::kills]))
        :ret string?)

(defn format-weapon-kills
    [row]
    (let [infantry-kills (:infantry-count row)
          vehicle-kills  (:vehicle-count row)
          weapon-name    (:item-name row)]
        (str weapon-name " - `" infantry-kills "`/`" vehicle-kills "`")))
(s/fdef format-weapon-kills
        :args (s/cat :row (s/keys :req [::infantry-count ::vehicle-count ::item-name]))
        :ret string?)

(defn get-xp-stats
    [char-info]
    (let [most-exp-first         (take 10 (get-char-stats-sorted (:experience-events char-info)))
          exp-list               (api/get-experience-types)
          exp-descriptions-added (map #(assoc % :description (get-in exp-list [(:experience-id %) :description])) most-exp-first)]
        exp-descriptions-added))
(s/fdef get-xp-stats
        :args (s/cat ::char-info map?)
        :ret seq?)

(defn print-stats
    [payload char-exp]

    (let [character-id              (:character-id payload)
          char-name                 (get-in (api/get-characters) [character-id :name :first])
          title                     (str char-name " Stats Summary (" character-id ")")
          char-info                 (get @char-exp character-id)
          ;xp-summary                (clojure.string/join "\n" (map format-exp-total (get-xp-stats char-info)))
          summary                   (get-overall-summary char-info)
          vehicle-destroyed-summary (clojure.string/join "\n" (map #(str "x" (:amount %1) " - " (:name %1)) (get-vehicle-kill-stats char-info)))
          vehicle-lost-summary      (clojure.string/join "\n" (map #(str "x" (:amount %1) " - " (:name %1)) (get-vehicle-lost-stats char-info)))
          kills-by-weapon           (clojure.string/join "\n" (map format-weapon-kills (get-kills-by-weapon char-info)))
          fields                    [;{:name "XP (Top 10)" :value xp-summary}
                                     {:name "Vehicles Destroyed" :value vehicle-destroyed-summary}
                                     {:name "Vehicles Lost" :value vehicle-lost-summary}
                                     {:name "Kills - By Weapon (Infantry/Vehicle)" :value kills-by-weapon}]]

        (if (not (empty? (:experience-events char-info)))
            (do
                (helper/log (str "sending summary for " char-name " (" character-id ")"))
                (discord/send-message title summary fields))

            (helper/log (str "no summary for " char-name " (" character-id ")")))))
