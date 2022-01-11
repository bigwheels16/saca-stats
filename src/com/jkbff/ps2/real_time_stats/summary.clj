(ns com.jkbff.ps2.real-time-stats.summary
    (:require [com.jkbff.ps2.real-time-stats.api :as api]
              [com.jkbff.ps2.real-time-stats.helper :as helper]
              [com.jkbff.ps2.real-time-stats.discord :as discord]
              [clojure.spec.alpha :as s]))

(defn format-float
    [input]
    (format "%.2f" (float input)))

(defn get-char-stats-sorted
    [experience-events]
    (let [grouped (group-by :experience-id experience-events)
          coll    (map (fn [[k v]] {:experience-id k :count (count v) :amount (apply + (map #(Integer/parseInt (:amount %)) v))}) grouped)]
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
        :args (s/cat :login-time (s/nilable number?))
        :ret number?)

(defn get-gunner-kills
    [char-info]
    (let [xp                (:experience-events char-info)
          mapped-to-vehicle (map #(assoc % :name (get-in api/gunner-experience [(:experience-id %) :killed])) xp)
          filtered          (filter :name mapped-to-vehicle)
          xp-grouped        (group-by :name filtered)
          mapped            (map (fn [[k v]] {:name k :amount (count v)}) xp-grouped)]
        (reverse (sort-by :amount mapped))))

(defn get-gunner-vehicles-destroyed
    [char-info]
    (let [gunner-kills         (get-gunner-kills char-info)
          vehicles-by-name     (api/get-vehicles-by-name)
          gunner-vehicle-kills (map #(let [name       (:name %)
                                           vehicle-id (:vehicle-id (get vehicles-by-name name))]
                                         (assoc % :vehicle-id vehicle-id)) gunner-kills)]
        (filter :vehicle-id gunner-vehicle-kills)))

(defn get-overall-summary
    [char-info]
    (let [total-time               (get-total-time (:logon char-info))
          total-xp                 (get-total-xp (:experience-events char-info))
          xp-per-min               (if total-time (quot (* total-xp 60 1000) total-time) "Unknown")
          num-kills                (count (:kills char-info))
          num-deaths               (count (:deaths char-info))
          kd                       (format-float (/ num-kills (if (zero? num-deaths) 1 num-deaths)))
          vehicle-map              (api/get-vehicles)
          num-facility-captured    (count (:facility-capture char-info))
          num-facility-defended    (count (:facility-defend char-info))
          nanites-used             (reduce + 0 (map #(get-in vehicle-map [(:vehicle-id %) :cost] 0) (:vehicle-deaths char-info)))
          nanites-destroyed        (reduce + 0 (map #(get-in vehicle-map [(:vehicle-id %) :cost] 0) (:vehicle-kills char-info)))
          gunner-nanites-destroyed (reduce + 0 (map #(* (:amount %) (get-in vehicle-map [(:vehicle-id %) :cost] 0)) (get-gunner-vehicles-destroyed char-info)))
          nanite-efficiency        (format-float (/ nanites-destroyed (if (zero? nanites-used) 1 nanites-used)))
          total-nanite-efficiency  (format-float (/ (+ nanites-destroyed gunner-nanites-destroyed) (if (zero? nanites-used) 1 nanites-used)))]
        (str "Time: " (if total-time (helper/get-time-str (quot total-time 1000)) "Unknown")
             "\nTotal XP: `" total-xp "` XP / min: `" xp-per-min "`"
             "\nKills: `" num-kills "` Deaths: `" num-deaths "` K/D: `" kd "`"
             "\nFacilities Defended: `" num-facility-defended "` Facilities Captured: `" num-facility-captured "`"
             "\nNanites Used: `" nanites-used "` Nanites Destroyed: `" nanites-destroyed "` Nanite Efficiency: `" nanite-efficiency "`"
             (if (> gunner-nanites-destroyed 0)
                 (str "\nGunner Nanites Destroyed: `" gunner-nanites-destroyed "` Total Nanite Efficiency: `" total-nanite-efficiency "`")))))

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
          mapped             (map (fn [[k v]] {:vehicle-id k :name (get-in vehicle-map [k :name]) :amount (count v)}) grouped)
          filtered           (filter #(get-in vehicle-map [(:vehicle-id %) :cost]) mapped)]
        (reverse (sort-by :amount filtered))))

(defn get-vehicle-lost-stats
    [char-info]
    (let [vehicles-lost (:vehicle-deaths char-info)
          grouped       (group-by :vehicle-id vehicles-lost)
          vehicle-map   (api/get-vehicles)
          mapped        (map (fn [[k v]] {:vehicle-id k :name (get-in vehicle-map [k :name]) :amount (count v)}) grouped)
          filtered      (filter #(get-in vehicle-map [(:vehicle-id %) :cost]) mapped)]
        (reverse (sort-by :amount filtered))))

(defn get-weapon-name
    [item-id]
    (if (= "0" item-id)
        "RAM"
        (or (:name (api/get-item-info item-id)) (str "Unknown(" item-id ")"))))

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

(defn format-weapon-kills
    [row]
    (let [infantry-kills (:infantry-count row)
          vehicle-kills  (:vehicle-count row)
          weapon-name    (:item-name row)]
        (str weapon-name " - `" infantry-kills "`, `" vehicle-kills "`")))

(defn get-xp-stats
    [char-info]
    (let [most-exp-first         (take 10 (get-char-stats-sorted (:experience-events char-info)))
          exp-list               (api/get-experience-types)
          exp-descriptions-added (map #(assoc % :description (get-in exp-list [(:experience-id %) :description])) most-exp-first)]
        exp-descriptions-added))

(defn get-title
    [char-details]
    (let [character-id (:character-id char-details)
          char-name    (get-in char-details [:name :first])
          faction-id   (:faction-id char-details)
          faction      (get-in (api/get-factions) [faction-id :code-tag])
          world-id     (or (:world-id char-details) (get-in char-details [:world :world-id]))
          world        (get-in (api/get-worlds) [world-id :name])]
        (str char-name " (" faction " " world ") - " character-id)))

(defn print-stats
    [character-id char-exp char-map]

    (let [char-details              (or (get char-map character-id) (api/get-character-by-id character-id))
          char-name                 (get-in char-details [:name :first])
          title                     (get-title char-details)
          char-info                 (get @char-exp character-id)
          ;xp-summary                (clojure.string/join "\n" (map format-exp-total (get-xp-stats char-info)))
          summary                   (get-overall-summary char-info)
          vehicle-destroyed-summary (clojure.string/join "\n" (map #(str "x" (:amount %1) " - " (:name %1)) (get-vehicle-kill-stats char-info)))
          vehicle-lost-summary      (clojure.string/join "\n" (map #(str "x" (:amount %1) " - " (:name %1)) (get-vehicle-lost-stats char-info)))
          kills-by-weapon           (clojure.string/join "\n" (map format-weapon-kills (get-kills-by-weapon char-info)))
          gunner-kills              (clojure.string/join "\n" (map #(str "x" (:amount %1) " - " (:name %1)) (get-gunner-kills char-info)))
          fields                    [;{:name "XP (Top 10)" :value xp-summary}
                                     {:name "Vehicles Destroyed" :value vehicle-destroyed-summary}
                                     {:name "Vehicles Lost" :value vehicle-lost-summary}
                                     {:name "Kills - By Weapon (Infantry, Vehicle)" :value kills-by-weapon}
                                     {:name "Gunner Kills" :value gunner-kills}]]

        (if (not (empty? (:experience-events char-info)))
            (do
                (helper/log (str "sending summary for " char-name " (" character-id ")"))
                (discord/send-message title summary fields))

            (helper/log (str "no summary for " char-name " (" character-id ")")))))
