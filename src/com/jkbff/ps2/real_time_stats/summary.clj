(ns com.jkbff.ps2.real-time-stats.summary
    (:require [com.jkbff.helper :as helper]
              [com.jkbff.ps2.real-time-stats.api :as api]
              [com.jkbff.ps2.real-time-stats.discord :as discord]
              [com.jkbff.ps2.events.dao.events :as events]
              [clojure.spec.alpha :as s]
              [clojure.tools.logging :as log]))


(defn format-float
    [input]
    (format "%.2f" (float input)))

(defn get-char-stats-sorted
    [experience-events]
    (let [grouped (group-by :GAIN_EXPERIENCE_EVENT/EXPERIENCE_ID experience-events)
          coll    (map (fn [[k v]] {:experience-id k :count (count v) :amount (apply + (map #(:GAIN_EXPERIENCE_EVENT/AMOUNT %) v))}) grouped)]
        (reverse (sort-by :amount coll))))

(defn format-exp-total
    [exp-total]
    (str (:amount exp-total) " (x" (:count exp-total) ") - " (or (:description exp-total) (:experience-id exp-total))))

(defn get-total-xp
    [experience-events]
    (reduce #(+ %1 (:GAIN_EXPERIENCE_EVENT/AMOUNT %2)) 0 experience-events))

(defn get-total-time
    [logon-time]
    (if logon-time
        (- (quot (System/currentTimeMillis) 1000) logon-time)
        1))
(s/fdef get-total-time
        :args (s/cat :login-time (s/nilable number?))
        :ret number?)

(defn get-gunner-kills
    [char-activity]
    (let [xp                (:xp char-activity)
          mapped-to-vehicle (map #(assoc % :name (get-in api/gunner-experience [(helper/int-to-string (:GAIN_EXPERIENCE_EVENT/EXPERIENCE_ID %)) :killed])) xp)
          filtered          (filter :name mapped-to-vehicle)
          xp-grouped        (group-by :name filtered)
          mapped            (map (fn [[k v]] {:name k :amount (count v)}) xp-grouped)]
        (reverse (sort-by :amount mapped))))

(defn get-gunner-vehicles-destroyed
    [char-activity]
    (let [gunner-kills         (get-gunner-kills char-activity)
          vehicles-by-name     (api/get-vehicles-by-name)
          gunner-vehicle-kills (map #(let [name       (:name %)
                                           vehicle-id (:vehicle-id (get vehicles-by-name name))]
                                         (assoc % :vehicle-id vehicle-id)) gunner-kills)]
        (filter :vehicle-id gunner-vehicle-kills)))

(defn get-overall-summary
    [char-activity]
    (let [login-time-obj           (first (:logon char-activity))
          total-time               (get-total-time (:TIMESTAMP login-time-obj))
          total-xp                 (get-total-xp (:xp char-activity))
          xp-per-min               (quot (* total-xp 60) total-time)
          num-kills                (count (:kills char-activity))
          num-deaths               (count (:deaths char-activity))
          kd                       (format-float (/ num-kills (if (zero? num-deaths) 1 num-deaths)))
          vehicle-map              (api/get-vehicles)
          num-facility-captured    (count (:facility-captures char-activity))
          num-facility-defended    (count (:facility-defends char-activity))
          nanites-used             (reduce + 0 (map #(get-in vehicle-map [(helper/int-to-string (:VEHICLE_DEATH_EVENT/CHARACTER_VEHICLE_ID %)) :cost] 0) (:vehicle-deaths char-activity)))
          nanites-destroyed        (reduce + 0 (map #(get-in vehicle-map [(helper/int-to-string (:VEHICLE_DESTROY_EVENT/CHARACTER_VEHICLE_ID %)) :cost] 0) (:vehicle-kills char-activity)))
          gunner-nanites-destroyed (reduce + 0 (map #(* (:amount %) (get-in vehicle-map [(helper/int-to-string (:vehicle-id %)) :cost] 0)) (get-gunner-vehicles-destroyed char-activity)))
          nanite-efficiency        (format-float (/ nanites-destroyed (if (zero? nanites-used) 1 nanites-used)))
          total-nanite-efficiency  (format-float (/ (+ nanites-destroyed gunner-nanites-destroyed) (if (zero? nanites-used) 1 nanites-used)))]

        (str "Time: " (str (helper/get-time-str total-time) (if (= 1 (:INFERRED login-time-obj)) "*"))
             "\nTotal XP: `" total-xp "` XP / min: `" xp-per-min "`"
             "\nKills: `" num-kills "` Deaths: `" num-deaths "` K/D: `" kd "`"
             "\nFacilities Defended: `" num-facility-defended "` Facilities Captured: `" num-facility-captured "`"
             "\nNanites Used: `" nanites-used "` Nanites Destroyed: `" nanites-destroyed "` Nanite Efficiency: `" nanite-efficiency "`"
             (if (> gunner-nanites-destroyed 0)
                 (str "\nGunner Nanites Destroyed: `" gunner-nanites-destroyed "` Total Nanite Efficiency: `" total-nanite-efficiency "`")))))

(defn get-max-kills
    [char-activity]
    (let [infantry-kills (:kills char-activity)
          grouped        (group-by :KILL_EVENT/LOADOUT_ID infantry-kills)
          infantry-map   (api/get-loadouts)
          mapped         (map (fn [[k v]] {:loadout-id k :name (get-in infantry-map [k :code-name]) :amount (count v)}) grouped)
          filtered       (filter #(case (:loadout-id %)
                                      "7" true
                                      "14" true
                                      "21" true
                                      false) mapped)]
        filtered))

(defn get-vehicle-kill-stats
    [char-activity]
    (let [vehicles-destroyed (filter #(not= "0" (:VEHICLE_DESTROY_EVENT/ATTACKER_VEHICLE_ID %)) (:vehicle-kills char-activity)) ; only count vehicles destroyed from a vehicle
          grouped            (group-by :VEHICLE_DESTROY_EVENT/CHARACTER_VEHICLE_ID vehicles-destroyed)
          vehicle-map        (api/get-vehicles)
          mapped             (map (fn [[k v]] {:vehicle-id k :name (get-in vehicle-map [(helper/int-to-string k) :name]) :amount (count v)}) grouped)
          filtered           (filter #(get-in vehicle-map [(helper/int-to-string (:vehicle-id %)) :cost]) mapped)]
        (reverse (sort-by :amount filtered))))

(defn get-vehicle-lost-stats
    [char-activity]
    (let [vehicles-lost (:vehicle-deaths char-activity)
          grouped       (group-by :VEHICLE_DEATH_EVENT/CHARACTER_VEHICLE_ID vehicles-lost)
          vehicle-map   (api/get-vehicles)
          mapped        (map (fn [[k v]] {:vehicle-id k :name (get-in vehicle-map [(helper/int-to-string k) :name]) :amount (count v)}) grouped)
          filtered      (filter #(get-in vehicle-map [(helper/int-to-string (:vehicle-id %)) :cost]) mapped)]
        (reverse (sort-by :amount filtered))))

(defn get-weapon-name
    [item-id]
    (if (= 0 item-id)
        "Ram/Crash/Fall"
        (or (:name (api/get-item-info item-id)) (str "Unknown(" item-id ")"))))

(defn get-kills-by-weapon
    [char-activity]
    (let [vehicles-destroyed (:vehicle-kills char-activity)
          vehicles-grouped   (group-by :VEHICLE_DESTROY_EVENT/ATTACKER_WEAPON_ID vehicles-destroyed)
          infantry-killed    (:kills char-activity)
          infantry-grouped   (group-by :KILL_EVENT/ATTACKER_WEAPON_ID infantry-killed)
          item-ids           (-> #{}
                                 (into (keys vehicles-grouped))
                                 (into (keys infantry-grouped)))
          mapped             (map #(hash-map :item-id %
                                             :item-name (get-weapon-name %)
                                             :vehicle-count (count (get vehicles-grouped % []))
                                             :infantry-count (count (get infantry-grouped % [])))
                                  item-ids)]

        (reverse (sort-by #(+ (:vehicle-count %) (:infantry-count %)) mapped))))

(defn format-weapon-kills
    [row]
    (let [infantry-kills (:infantry-count row)
          vehicle-kills  (:vehicle-count row)
          weapon-name    (:item-name row)]
        (str weapon-name " - `" infantry-kills "`, `" vehicle-kills "`")))

(defn get-xp-stats
    [char-activity]
    (let [most-exp-first         (take 10 (get-char-stats-sorted (:xp char-activity)))
          exp-list               (api/get-experience-types)
          exp-descriptions-added (map #(assoc % :description (get-in exp-list [(helper/int-to-string (:experience-id %)) :description])) most-exp-first)]
        exp-descriptions-added))

(defn get-title
    [character-id char-details]
    (let [char-name    (get-in char-details [:name :first])
          faction-id   (:faction-id char-details)
          faction      (get-in (api/get-factions) [faction-id :code-tag])
          world-id     (or (:world-id char-details) (get-in char-details [:world :world-id]))
          world        (get-in (api/get-worlds) [world-id :name])]
        (str char-name " (" faction " " world ") - " character-id)))

(defn print-stats
    [ds character-id]

    (let [char-details              (api/get-character-by-id character-id)
          char-name                 (get-in char-details [:name :first])
          title                     (get-title character-id char-details)
          char-activity             (events/get-char-activity ds character-id)
          ;xp-summary                (clojure.string/join "\n" (map format-exp-total (get-xp-stats char-activity)))
          summary                   (get-overall-summary char-activity)
          vehicle-destroyed-summary (clojure.string/join "\n" (map #(str "x" (:amount %1) " - " (:name %1)) (get-vehicle-kill-stats char-activity)))
          vehicle-lost-summary      (clojure.string/join "\n" (map #(str "x" (:amount %1) " - " (:name %1)) (get-vehicle-lost-stats char-activity)))
          kills-by-weapon           (clojure.string/join "\n" (map format-weapon-kills (get-kills-by-weapon char-activity)))
          gunner-kills              (clojure.string/join "\n" (map #(str "x" (:amount %1) " - " (:name %1)) (get-gunner-kills char-activity)))
          fields                    [;{:name "XP (Top 10)" :value xp-summary}
                                     {:name "Vehicles Destroyed" :value vehicle-destroyed-summary}
                                     {:name "Vehicles Lost" :value vehicle-lost-summary}
                                     {:name "Kills - By Weapon (Infantry, Vehicle)" :value kills-by-weapon}
                                     {:name "Gunner Kills" :value gunner-kills}]]

        (if (not (empty? (:xp char-activity)))
            (do
                (log/info (str "sending summary for " char-name " (" character-id ")"))
                (discord/send-message title summary fields))

            (log/info (str "no summary for " char-name " (" character-id ")")))))
