(ns com.jkbff.ps2.real_time_stats.core
    (:require [gniazdo.core :as ws]
              [com.jkbff.ps2.real_time_stats.helper :as helper]
              [com.jkbff.ps2.real_time_stats.config :as config]
              [clj-http.client :as client]))

(def char-exp (atom {}))

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
                          list   (helper/read-json (:body result))
                          m      (reduce #(assoc %1 (:experience-id %2) %2) {} (:experience-list list))]
                        m))))

(def get-character-names
    (memoize (fn [character-ids] (let [result (client/get (str "http://census.daybreakgames.com/s:" (config/SERVICE_ID) "/get/ps2:v2/character/?character_id=" (config/SUBSCRIBE_CHARACTER_IDS)))]
                                     ))))

(defn update-experience
    [char-exp-map payload]

    (let [character-id  (:character-id payload)
          experience-id (:experience-id payload)
          amount        (Integer/parseInt (:amount payload))
          current-val   (or (get char-exp-map character-id) (list))
          new-val       (cons {:experience-id experience-id :amount amount} current-val)]

        (assoc char-exp-map character-id new-val)))

(defn process-char-info
    [m char-info]

    (let [{experience-id :experience-id amount :amount} char-info]
        (update m experience-id (fn [{current-amount :amount current-count :count}]
                                    {:amount (+ (or current-amount 0) amount) :count (inc (or current-count 0)) :experience-id experience-id}))))

(defn get-char-stats-sorted
    [char-info]

    (let [processed (reduce process-char-info {} char-info)
          coll      (vals processed)]

        (reverse (sort-by :amount coll))))

(defn print-summary
    [payload]

    (let [character-id           (:character-id payload)
          char-info              (get @char-exp character-id)
          most-exp-first         (take 10 (get-char-stats-sorted char-info))
          exp-list               (get-experience-types)
          exp-descriptions-added (map #(assoc % :description (get-in exp-list [(:experience-id %) :description])) most-exp-first)]

        (println "printing summary for" character-id)
        (doseq [x exp-descriptions-added]
            (println x))))

(defn handle-message
    [msg]
    (let [obj     (helper/read-json msg)
          payload (:payload obj)]

        ;(if (not= "heartbeat" (:type obj))
        ;    (println obj))

        (case (:event-name payload)
            "GainExperience" (swap! char-exp update-experience payload)
            "PlayerLogout" (print-summary payload)
            nil)))

(defn connect
    []
    (->
        (ws/connect (str "wss://push.planetside2.com/streaming?environment=ps2&service-id=s:" (config/SERVICE_ID))
                    :on-receive handle-message)

        (ws/send-msg (helper/write-json {:service    "event"
                                         :action     "subscribe"
                                         :characters (config/SUBSCRIBE_CHARACTER_IDS)
                                         :eventNames (config/SUBSCRIBE_EVENTS)}))))

(defn -main
    [& args]

    (let [is-running true
          web-socket (connect)]

        (while is-running
            (Thread/sleep 1000))))
