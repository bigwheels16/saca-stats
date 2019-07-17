(ns com.jkbff.ps2.real_time_stats.summary
    (:require [com.jkbff.ps2.real_time_stats.api :as api]
              [com.jkbff.ps2.real_time_stats.helper :as helper]
              [com.jkbff.ps2.real_time_stats.discord :as discord]))

(defn process-char-info
    [m experience-events]

    (let [{experience-id :experience-id amount :amount} experience-events]
        (update m experience-id (fn [{current-amount :amount current-count :count}]
                                    {:amount (+ (or current-amount 0) amount) :count (inc (or current-count 0)) :experience-id experience-id}))))

(defn get-char-stats-sorted
    [experience-events]

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

(defn get-overall-summary
    [char-info]
    (let [total-time        (get-total-time (:logon char-info))
          total-xp          (get-total-xp (:experience-events char-info))
          xp-per-min        (if total-time (quot (* total-xp 60 1000) total-time) "Unknown")
          num-kills         (count (:kills char-info))
          num-deaths        (count (:deaths char-info))
          kd                (float (/ num-kills (if (zero? num-deaths) 1 num-deaths)))
          vehicle-map       (api/get-vehicles)
          nanites-used      (reduce + 0 (map #(get-in vehicle-map [(:vehicle-id %) :cost] 0) (:vehicle-deaths char-info)))
          nanites-destroyed (reduce + 0 (map #(get-in vehicle-map [(:vehicle-id %) :cost] 0) (:vehicle-kills char-info)))
          nanite-efficiency (float (/ nanites-destroyed (if (zero? nanites-used) 1 nanites-used)))]
        (str "Time: " (if total-time (helper/get-time-str (quot total-time 1000)) "Unknown")
             "\nTotal XP: `" total-xp "` XP / min: `" xp-per-min "`"
             "\nKills: `" num-kills "` Deaths: `" num-deaths "` K/D: `" kd "`"
             "\nNanites Used: `" nanites-used "` Nanites Destroyed: `" nanites-destroyed "` Nanite Efficiency: `" nanite-efficiency "`")))

(defn get-vehicle-kill-stats
    [char-info]
    (let [vehicles-destroyed (:vehicle-kills char-info)
          grouped            (group-by :vehicle-id vehicles-destroyed)
          vehicle-map        (api/get-vehicles)
          mapped             (map (fn [[k v]] {:vehicle-id k :name (get-in vehicle-map [k :name :en]) :amount (count v)}) grouped)]
        (reverse (sort-by :amount mapped))))

(defn get-vehicle-lost-stats
    [char-info]
    (let [vehicles-destroyed (:vehicle-deaths char-info)
          grouped            (group-by :vehicle-id vehicles-destroyed)
          vehicle-map        (api/get-vehicles)
          mapped             (map (fn [[k v]] {:vehicle-id k :name (get-in vehicle-map [k :name :en]) :amount (count v)}) grouped)]
        (reverse (sort-by :amount mapped))))

(defn print-stats
    [payload char-exp]

    (let [character-id              (:character-id payload)
          char-name                 (get-in (api/get-characters) [character-id :name :first])
          title                     (str char-name " Stats Summary (" character-id ")")
          char-info                 (get @char-exp character-id)
          most-exp-first            (take 10 (get-char-stats-sorted (:experience-events char-info)))
          exp-list                  (api/get-experience-types)
          exp-descriptions-added    (map #(assoc % :description (get-in exp-list [(:experience-id %) :description])) most-exp-first)
          summary                   (get-overall-summary char-info)
          xp-summary                (clojure.string/join "\n" (map format-exp-total exp-descriptions-added))
          vehicle-destroyed-summary (clojure.string/join "\n" (map #(str "x" (:amount %1) " - " (:name %1)) (get-vehicle-kill-stats char-info)))
          vehicle-lost-summary      (clojure.string/join "\n" (map #(str "x" (:amount %1) " - " (:name %1)) (get-vehicle-lost-stats char-info)))
          fields                    [{:name "XP (Top 10)" :value xp-summary}
                                     {:name "Vehicles Destroyed" :value vehicle-destroyed-summary}
                                     {:name "Vehicles Lost" :value vehicle-lost-summary}]]

        (helper/log (str "sending summary for " char-name " (" character-id ")"))
        (discord/send-message title summary fields)))