(ns com.jkbff.ps2.real_time_stats.api
    (:require [clj-http.client :as client]
              [com.jkbff.ps2.real_time_stats.helper :as helper]
              [com.jkbff.ps2.real_time_stats.config :as config]))

(def vehicle-costs {"1"  {:cost 50}                         ; flash
                    "2"  {:cost 200}                        ; sunderer
                    "3"  {:cost 350}                        ; lightning
                    "4"  {:cost 450}                        ; magrider
                    "5"  {:cost 450}                        ; vanguard
                    "6"  {:cost 450}                        ; prowler
                    "7"  {:cost 350}                        ; scythe
                    "8"  {:cost 350}                        ; reaver
                    "9"  {:cost 350}                        ; mosquito
                    "10" {:cost 450}                        ; liberator
                    "11" {:cost 450}                        ; galaxy
                    "12" {:cost 150}                        ; harasser
                    "14" {:cost 250}                        ; valkyrie
                    "15" {:cost 200}                        ; ant
                    })

(def get-experience-types
    (memoize (fn [] (let [result   (client/get (str "https://census.daybreakgames.com/s:" (config/SERVICE_ID) "/get/ps2/experience?c:limit=2000"))
                          body     (helper/read-json (:body result))
                          coll     (:experience-list body)]
                        (zipmap (map :experience-id coll) coll)))))

(def get-characters
    (memoize (fn []
                 (let [char-names-lower (map #(clojure.string/trim (clojure.string/lower-case %)) (config/SUBSCRIBE_CHARACTERS))
                       char-names-str   (clojure.string/join "," char-names-lower)
                       url              (str "http://census.daybreakgames.com/s:" (config/SERVICE_ID) "/get/ps2/character?name.first_lower=" char-names-str "&c:limit=" (count char-names-lower))
                       result           (client/get url)
                       body             (helper/read-json (:body result))
                       coll             (:character-list body)]
                     (zipmap (map :character-id coll) coll)))))

(def get-vehicles
    (memoize (fn []
                 (let [url          (str "http://census.daybreakgames.com/s:" (config/SERVICE_ID) "/get/ps2:v2/vehicle?c:limit=500&c:lang=en")
                       result       (client/get url)
                       body         (helper/read-json (:body result))
                       coll         (map #(select-keys % [:vehicle-id :name]) (:vehicle-list body))
                       m            (zipmap (map :vehicle-id coll) coll)]
                     (helper/deep-merge m vehicle-costs)))))

(def get-continents
    (memoize (fn []
                 (let [url       (str "http://census.daybreakgames.com/s:" (config/SERVICE_ID) "/get/ps2:v2/zone?c:limit=500&c:lang=en")
                       result    (client/get url)
                       body      (helper/read-json (:body result))
                       coll      (:zone-list body)]
                     (zipmap (map :zone-id coll) coll)))))

(def get-worlds
    (memoize (fn []
                 (let [url        (str "http://census.daybreakgames.com/s:" (config/SERVICE_ID) "/get/ps2:v2/world?c:limit=100&c:lang=en")
                       result     (client/get url)
                       body       (helper/read-json (:body result))
                       coll       (:world-list body)]
                     (zipmap (map :world-id coll) coll)))))

(def get-loadouts
    (memoize (fn []
                 (let [url          (str "http://census.daybreakgames.com/s:" (config/SERVICE_ID) "/get/ps2:v2/loadout?c:limit=500&c:lang=en")
                       result       (client/get url)
                       body         (helper/read-json (:body result))
                       coll         (:loadout-list body)]
                     (zipmap (map :loadout-id coll) coll)))))
