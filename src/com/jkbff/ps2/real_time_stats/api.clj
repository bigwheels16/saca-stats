(ns com.jkbff.ps2.real_time_stats.api
    (:require [clj-http.client :as client]
              [com.jkbff.ps2.real_time_stats.helper :as helper]
              [com.jkbff.ps2.real_time_stats.config :as config]))

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

(def get-continents
    (memoize (fn []
                 (let [url          (str "http://census.daybreakgames.com/s:" (config/SERVICE_ID) "/get/ps2:v2/zone?c:limit=500&c:lang=en")
                       result       (client/get url)
                       body         (helper/read-json (:body result))
                       zone-list (:zone-list body)]
                     (zipmap (map :zone-id zone-list) zone-list)))))

(def get-worlds
    (memoize (fn []
                 (let [url          (str "http://census.daybreakgames.com/s:" (config/SERVICE_ID) "/get/ps2:v2/world?c:limit=100&c:lang=en")
                       result       (client/get url)
                       body         (helper/read-json (:body result))
                       world-list   (:world-list body)]
                     (zipmap (map :world-id world-list) world-list)))))
