(ns com.jkbff.ps2.real-time-stats.api
    (:require [clj-http.client :as client]
              [com.jkbff.ps2.real-time-stats.helper :as helper]
              [com.jkbff.ps2.real-time-stats.config :as config]))

(def LANG :en)
(def BASE-URL (str "https://census.daybreakgames.com/s:" (config/SERVICE_ID) "/get/ps2:v2"))
(defn create-url
    [collection-type query-string]
    (str BASE-URL "/" collection-type "?" (clojure.string/join "&" (map (fn [[k v]] (str k "=" v)) (partition 2 query-string)))))

(def mbt-cost 450)
(def esf-cost 350)

(def vehicle-costs
    {"1"  {:cost 50}                                        ; flash
     "2"  {:cost 200}                                       ; sunderer
     "3"  {:cost 300}                                       ; lightning
     "4"  {:cost mbt-cost}                                  ; magrider
     "5"  {:cost mbt-cost}                                  ; vanguard
     "6"  {:cost mbt-cost}                                  ; prowler
     "7"  {:cost esf-cost}                                  ; scythe
     "8"  {:cost esf-cost}                                  ; reaver
     "9"  {:cost esf-cost}                                  ; mosquito
     "10" {:cost 450}                                       ; liberator
     "11" {:cost 450}                                       ; galaxy
     "12" {:cost 300}                                       ; harasser
     "14" {:cost 250}                                       ; valkyrie
     "15" {:cost 200}                                       ; ant
     "2007" {:cost 0}                                       ; colossus
     "2010" {:cost 0}                                       ; flash xs-1
     "2019" {:cost 0}                                       ; bastion fleet carrier
     "2033" {:cost 100}                                     ; javelin
     "2122" {:cost esf-cost}                                ; mosquito interceptor
     "2123" {:cost esf-cost}                                ; reaver interceptor
     "2124" {:cost esf-cost}                                ; scythe interceptor
     "2125" {:cost 100}                                     ; javelin
     "2129" {:cost 100}                                     ; javelin
     "2130" {:cost 0}                                       ; reclaimed sunderer
     "2131" {:cost 0}                                       ; reclaimed galaxy
     "2132" {:cost 0}                                       ; reclaimed valkyrie
     "2133" {:cost mbt-cost}                                ; reclaimed magrider
     "2134" {:cost mbt-cost}                                ; reclaimed vanguard
     "2135" {:cost mbt-cost}                                ; reclaimed prowler
     "2136" {:cost esf-cost}                                ; dervish
     })

; missing ANT Kill by Sunderer
(def gunner-experience
    {"146" {:killed "Player"
            :by     "Sunderer"}
     "159" {:killed "Flash"
            :by     "Sunderer"}
     "160" {:killed "Sunderer"
            :by     "Sunderer"}
     "161" {:killed "Lightning"
            :by     "Sunderer"}
     "162" {:killed "Magrider"
            :by     "Sunderer"}
     "163" {:killed "Vanguard"
            :by     "Sunderer"}
     "164" {:killed "Prowler"
            :by     "Sunderer"}
     "165" {:killed "Scythe"
            :by     "Sunderer"}
     "166" {:killed "Reaver"
            :by     "Sunderer"}
     "167" {:killed "Mosquito"
            :by     "Sunderer"}
     "168" {:killed "Liberator"
            :by     "Sunderer"}
     "169" {:killed "Galaxy"
            :by     "Sunderer"}
     "308" {:killed "Harasser"
            :by     "Sunderer"}
     "509" {:killed "Valkyrie"
            :by     "Sunderer"}

     "148" {:killed "Player"
            :by     "Magrider"}
     "181" {:killed "Flash"
            :by     "Magrider"}
     "182" {:killed "Sunderer"
            :by     "Magrider"}
     "183" {:killed "Lightning"
            :by     "Magrider"}
     "184" {:killed "Vanguard"
            :by     "Magrider"}
     "185" {:killed "Prowler"
            :by     "Magrider"}
     "186" {:killed "Reaver"
            :by     "Magrider"}
     "187" {:killed "Mosquito"
            :by     "Magrider"}
     "188" {:killed "Liberator"
            :by     "Magrider"}
     "189" {:killed "Galaxy"
            :by     "Magrider"}
     "309" {:killed "Harasser"
            :by     "Magrider"}
     "510" {:killed "Valkyrie"
            :by     "Magrider"}
     "658" {:killed "ANT"
            :by     "Magrider"}

     "149" {:killed "Player"
            :by     "Vanguard"}
     "190" {:killed "Flash"
            :by     "Vanguard"}
     "191" {:killed "Sunderer"
            :by     "Vanguard"}
     "192" {:killed "Lightning"
            :by     "Vanguard"}
     "193" {:killed "Magrider"
            :by     "Vanguard"}
     "195" {:killed "Prowler"
            :by     "Vanguard"}
     "196" {:killed "Scythe"
            :by     "Vanguard"}
     "197" {:killed "Mosquito"
            :by     "Vanguard"}
     "198" {:killed "Liberator"
            :by     "Vanguard"}
     "199" {:killed "Galaxy"
            :by     "Vanguard"}
     "310" {:killed "Harasser"
            :by     "Vanguard"}
     "511" {:killed "Valkyrie"
            :by     "Vanguard"}
     "659" {:killed "ANT"
            :by     "Vanguard"}

     "150" {:killed "Player"
            :by     "Prowler"}
     "200" {:killed "Flash"
            :by     "Prowler"}
     "202" {:killed "Sunderer"
            :by     "Prowler"}
     "203" {:killed "Lightning"
            :by     "Prowler"}
     "204" {:killed "Magrider"
            :by     "Prowler"}
     "205" {:killed "Vanguard"
            :by     "Prowler"}
     "207" {:killed "Scythe"
            :by     "Prowler"}
     "208" {:killed "Reaver"
            :by     "Prowler"}
     "209" {:killed "Liberator"
            :by     "Prowler"}
     "210" {:killed "Galaxy"
            :by     "Prowler"}
     "311" {:killed "Harasser"
            :by     "Prowler"}
     "512" {:killed "Valkyrie"
            :by     "Prowler"}
     "660" {:killed "ANT"
            :by     "Prowler"}

     "154" {:killed "Player"
            :by     "Liberator"}
     "211" {:killed "Flash"
            :by     "Liberator"}
     "212" {:killed "Sunderer"
            :by     "Liberator"}
     "213" {:killed "Lightning"
            :by     "Liberator"}
     "214" {:killed "Magrider"
            :by     "Liberator"}
     "215" {:killed "Vanguard"
            :by     "Liberator"}
     "216" {:killed "Prowler"
            :by     "Liberator"}
     "217" {:killed "Scythe"
            :by     "Liberator"}
     "218" {:killed "Reaver"
            :by     "Liberator"}
     "219" {:killed "Mosquito"
            :by     "Liberator"}
     "220" {:killed "Liberator"
            :by     "Liberator"}
     "221" {:killed "Galaxy"
            :by     "Liberator"}
     "312" {:killed "Harasser"
            :by     "Liberator"}
     "513" {:killed "Valkyrie"
            :by     "Liberator"}
     "661" {:killed "ANT"
            :by     "Liberator"}

     "155" {:killed "Player"
            :by     "Galaxy"}
     "222" {:killed "Flash"
            :by     "Galaxy"}
     "223" {:killed "Sunderer"
            :by     "Galaxy"}
     "224" {:killed "Lightning"
            :by     "Galaxy"}
     "225" {:killed "Magrider"
            :by     "Galaxy"}
     "226" {:killed "Vanguard"
            :by     "Galaxy"}
     "227" {:killed "Prowler"
            :by     "Galaxy"}
     "228" {:killed "Scythe"
            :by     "Galaxy"}
     "229" {:killed "Reaver"
            :by     "Galaxy"}
     "230" {:killed "Mosquito"
            :by     "Galaxy"}
     "231" {:killed "Liberator"
            :by     "Galaxy"}
     "232" {:killed "Galaxy"
            :by     "Galaxy"}
     "313" {:killed "Harasser"
            :by     "Galaxy"}
     "514" {:killed "Valkyrie"
            :by     "Galaxy"}
     "662" {:killed "ANT"
            :by     "Galaxy"}

     "314" {:killed "Player"
            :by     "Harasser"}
     "315" {:killed "Flash"
            :by     "Harasser"}
     "316" {:killed "Sunderer"
            :by     "Harasser"}
     "317" {:killed "Lightning"
            :by     "Harasser"}
     "318" {:killed "Vanguard"
            :by     "Harasser"}
     "319" {:killed "Prowler"
            :by     "Harasser"}
     "320" {:killed "Reaver"
            :by     "Harasser"}
     "321" {:killed "Mosquito"
            :by     "Harasser"}
     "322" {:killed "Liberator"
            :by     "Harasser"}
     "323" {:killed "Galaxy"
            :by     "Harasser"}
     "324" {:killed "Harasser"
            :by     "Harasser"}
     "325" {:killed "Magrider"
            :by     "Harasser"}
     "326" {:killed "Scythe"
            :by     "Harasser"}
     "576" {:killed "Valkyrie"
            :by     "Harasser"}
     "666" {:killed "ANT"
            :by     "Harasser"}

     "515" {:killed "Player"
            :by     "Valkyrie"}
     "520" {:killed "Flash"
            :by     "Valkyrie"}
     "521" {:killed "Sunderer"
            :by     "Valkyrie"}
     "522" {:killed "Lightning"
            :by     "Valkyrie"}
     "523" {:killed "Vanguard"
            :by     "Valkyrie"}
     "524" {:killed "Prowler"
            :by     "Valkyrie"}
     "525" {:killed "Reaver"
            :by     "Valkyrie"}
     "526" {:killed "Mosquito"
            :by     "Valkyrie"}
     "527" {:killed "Liberator"
            :by     "Valkyrie"}
     "528" {:killed "Galaxy"
            :by     "Valkyrie"}
     "529" {:killed "Magrider"
            :by     "Valkyrie"}
     "530" {:killed "Scythe"
            :by     "Valkyrie"}
     "533" {:killed "Valkyrie"
            :by     "Valkyrie"}
     "575" {:killed "Harasser"
            :by     "Valkyrie"}
     "671" {:killed "ANT"
            :by     "Valkyrie"}

     "657" {:killed "ANT"
            :by     "ANT"}
     "676" {:killed "Flash"
            :by     "ANT"}
     "677" {:killed "Galaxy"
            :by     "ANT"}
     "678" {:killed "Harasser"
            :by     "ANT"}
     "679" {:killed "Magrider"
            :by     "ANT"}
     "680" {:killed "Mosquito"
            :by     "ANT"}
     "681" {:killed "Player"
            :by     "ANT"}
     "682" {:killed "Prowler"
            :by     "ANT"}
     "685" {:killed "Reaver"
            :by     "ANT"}
     "686" {:killed "Scythe"
            :by     "ANT"}
     "688" {:killed "Sunderer"
            :by     "ANT"}
     "689" {:killed "Valkyrie"
            :by     "ANT"}
     "690" {:killed "Vanguard"
            :by     "ANT"}
     })

(def get-experience-types
    (memoize (fn [] (let [result (client/get (create-url "experience" ["c:limit" "2000"]))
                          body   (helper/read-json (:body result))
                          coll   (:experience-list body)]
                        (zipmap (map :experience-id coll) coll)))))

(def get-characters
    (memoize (fn [char-names]
                 (if (< 0 (count char-names))
                     (let [char-names-lower (map #(clojure.string/trim (clojure.string/lower-case %)) char-names)
                           char-names-str   (clojure.string/join "," char-names-lower)
                           url              (create-url "character" ["name.first_lower" char-names-str
                                                                     "c:resolve" "world"
                                                                     "c:resolve" "outfit"
                                                                     "c:limit" (count char-names-lower)])
                           result           (client/get url)
                           body             (helper/read-json (:body result))]

                         (:character-list body))

                     ; else
                    []))))

(def get-character-by-id
    (memoize (fn [character-id]
                 (let [url              (create-url "character" ["character_id" character-id
                                                                 "c:resolve" "world"
                                                                 "c:resolve" "outfit"])
                       result           (client/get url)
                       body             (helper/read-json (:body result))]

                     (first (:character-list body))))))

(def get-vehicles
    (memoize (fn []
                 (let [url    (create-url "vehicle" ["c:limit" "500"
                                                     "c:lang" "en"])
                       result (client/get url)
                       body   (helper/read-json (:body result))
                       coll   (map #(hash-map :vehicle-id (:vehicle-id %) :name (get-in % [:name LANG])) (:vehicle-list body))
                       m      (zipmap (map :vehicle-id coll) coll)]
                     (helper/deep-merge m vehicle-costs)))))

(def get-vehicles-by-name
    (memoize (fn []
                 (let [vehicles (vals (get-vehicles))
                       m        (zipmap (map :name vehicles) vehicles)]
                     m))))

(def get-continents
    (memoize (fn []
                 (let [url    (create-url "zone" ["c:limit" "500"
                                                  "c:lang" "en"])
                       result (client/get url)
                       body   (helper/read-json (:body result))
                       coll   (map #(assoc % :name (get-in % [:name LANG])) (:zone-list body))]
                     (zipmap (map :zone-id coll) coll)))))

(def get-worlds
    (memoize (fn []
                 (let [url    (create-url "world" ["c:limit" "100"
                                                   "c:lang" "en"])
                       result (client/get url)
                       body   (helper/read-json (:body result))
                       coll   (map #(assoc % :name (get-in % [:name LANG])) (:world-list body))]
                     (zipmap (map :world-id coll) coll)))))

(def get-factions
    (memoize (fn []
                 (let [url    (create-url "faction" ["c:limit" "100"
                                                     "c:lang" "en"])
                       result (client/get url)
                       body   (helper/read-json (:body result))
                       coll   (map #(assoc % :name (get-in % [:name LANG])) (:faction-list body))]
                     (zipmap (map :faction-id coll) coll)))))

(def get-loadouts
    (memoize (fn []
                 (let [url    (create-url "loadout" ["c:limit" "500"
                                                     "c:lang" "en"])
                       result (client/get url)
                       body   (helper/read-json (:body result))
                       coll   (:loadout-list body)]
                     (zipmap (map :loadout-id coll) coll)))))

(def get-item-info
    (memoize (fn [item_id]
                 (let [url    (create-url "item" ["item_id" item_id
                                                  "c:lang" "en"])
                       result (client/get url)
                       body   (helper/read-json (:body result))
                       coll   (map #(assoc % :name (get-in % [:name LANG])) (:item-list body))]
                     (first coll)))))

(defn get-outfit-by-id
    [outfit-id]
    (let [url    (create-url "outfit" ["outfit_id" outfit-id
                                       "c:resolve" "member_character"
                                       "c:join" "type:characters_world^on:members.character_id^to:character_id^inject_at:world"])
          result (client/get url)
          body   (helper/read-json (:body result))]
        (first (:outfit-list body))))
