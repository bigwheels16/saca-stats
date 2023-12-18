(ns com.jkbff.ps2.real-time-stats.api
    (:require [clj-http.client :as client]
              [com.jkbff.helper :as helper]
              [com.jkbff.ps2.real-time-stats.config :as config]))

(def LANG :en)
(def BASE-URL (str "https://census.daybreakgames.com/s:" (config/SERVICE_ID) "/get/ps2:v2"))
(defn create-url
    [collection-type query-string]
    (str BASE-URL "/" collection-type "?" (clojure.string/join "&" (map (fn [[k v]] (str k "=" v)) (partition 2 query-string)))))

(def mbt-cost 450)
(def esf-cost 350)

; https://census.daybreakgames.com/get/ps2:v2/vehicle?c:limit=2000
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
     ;"2122" {:cost esf-cost}                                ; mosquito interceptor
     ;"2123" {:cost esf-cost}                                ; reaver interceptor
     ;"2124" {:cost esf-cost}                                ; scythe interceptor
     "2125" {:cost 100}                                     ; javelin *no usage seen
     "2129" {:cost 100}                                     ; javelin *no usage seen
     ;"2130" {:cost 0}                                       ; reclaimed sunderer
     ;"2131" {:cost 0}                                       ; reclaimed galaxy
     ;"2132" {:cost 0}                                       ; reclaimed valkyrie
     ;"2133" {:cost mbt-cost}                                ; reclaimed magrider
     ;"2134" {:cost mbt-cost}                                ; reclaimed vanguard
     ;"2135" {:cost mbt-cost}                                ; reclaimed prowler
     "2136" {:cost esf-cost}                                ; dervish
     "2137" {:cost mbt-cost}                                ; chimera
     "2142" {:cost 250}                                     ; corsair
     })

; https://census.daybreakgames.com/get/ps2:v2/experience?c:limit=2000
(def gunner-experience {
       "146"  {:killed "Player"      :by "Sunderer"}
       ;"0000" {:killed "ANT"         :by "Sunderer"}
       ;"0000" {:killed "Chimera"     :by "Sunderer"}
       ;"0000" {:killed "Colossus"    :by "Sunderer"}
       "1995" {:killed "Corsair"     :by "Sunderer"}
       "1640" {:killed "Dervish"     :by "Sunderer"}
       "159"  {:killed "Flash"       :by "Sunderer"}
       "169"  {:killed "Galaxy"      :by "Sunderer"}
       "308"  {:killed "Harasser"    :by "Sunderer"}
       "1486" {:killed "Javelin"     :by "Sunderer"}
       "168"  {:killed "Liberator"   :by "Sunderer"}
       "161"  {:killed "Lightning"   :by "Sunderer"}
       "162"  {:killed "Magrider"    :by "Sunderer"}
       "167"  {:killed "Mosquito"    :by "Sunderer"}
       "164"  {:killed "Prowler"     :by "Sunderer"}
       "166"  {:killed "Reaver"      :by "Sunderer"}
       "165"  {:killed "Scythe"      :by "Sunderer"}
       "160"  {:killed "Sunderer"    :by "Sunderer"}
       "163"  {:killed "Vanguard"    :by "Sunderer"}
       "509"  {:killed "Valkyrie"    :by "Sunderer"}

       "148"  {:killed "Player"      :by "Magrider"}
       "658"  {:killed "ANT"         :by "Magrider"}
       "1733" {:killed "Chimera"     :by "Magrider"}
       ;"0000" {:killed "Colossus"    :by "Magrider"}
       ;"0000" {:killed "Corsair"     :by "Magrider"}
       ;"0000" {:killed "Dervish"     :by "Magrider"}
       "181"  {:killed "Flash"       :by "Magrider"}
       "189"  {:killed "Galaxy"      :by "Magrider"}
       "309"  {:killed "Harasser"    :by "Magrider"}
       "1487" {:killed "Javelin"     :by "Magrider"}
       "188"  {:killed "Liberator"   :by "Magrider"}
       "183"  {:killed "Lightning"   :by "Magrider"}
       ;"0000" {:killed "Magrider"    :by "Magrider"}*
       "187"  {:killed "Mosquito"    :by "Magrider"}
       "185"  {:killed "Prowler"     :by "Magrider"}
       "186"  {:killed "Reaver"      :by "Magrider"}
       ;"0000" {:killed "Scythe"      :by "Magrider"}*
       "182"  {:killed "Sunderer"    :by "Magrider"}
       "184"  {:killed "Vanguard"    :by "Magrider"}
       "510"  {:killed "Valkyrie"    :by "Magrider"}

       "149"  {:killed "Player"      :by "Vanguard"}
       "659"  {:killed "ANT"         :by "Vanguard"}
       "1734" {:killed "Chimera"     :by "Vanguard"}
       ;"0000" {:killed "Colossus"    :by "Vanguard"}
       "1996" {:killed "Corsair"     :by "Vanguard"}
       "1641" {:killed "Dervish"     :by "Vanguard"}
       "190"  {:killed "Flash"       :by "Vanguard"}
       "199"  {:killed "Galaxy"      :by "Vanguard"}
       "310"  {:killed "Harasser"    :by "Vanguard"}
       "1488" {:killed "Javelin"     :by "Vanguard"}
       "198"  {:killed "Liberator"   :by "Vanguard"}
       "192"  {:killed "Lightning"   :by "Vanguard"}
       "193"  {:killed "Magrider"    :by "Vanguard"}
       "197"  {:killed "Mosquito"    :by "Vanguard"}
       "195"  {:killed "Prowler"     :by "Vanguard"}
       ;"0000" {:killed "Reaver"      :by "Vanguard"}*
       "196"  {:killed "Scythe"      :by "Vanguard"}
       "191"  {:killed "Sunderer"    :by "Vanguard"}
       "511"  {:killed "Valkyrie"    :by "Vanguard"}
       ;"0000" {:killed "Vanguard"    :by "Vanguard"}*

       "150"  {:killed "Player"      :by "Prowler"}
       "660"  {:killed "ANT"         :by "Prowler"}
       "1732" {:killed "Chimera"     :by "Prowler"}
       ;"0000" {:killed "Colossus"    :by "Prowler"}
       "1997" {:killed "Corsair"     :by "Prowler"}
       "1642" {:killed "Dervish"     :by "Prowler"}
       "200"  {:killed "Flash"       :by "Prowler"}
       "210"  {:killed "Galaxy"      :by "Prowler"}
       "311"  {:killed "Harasser"    :by "Prowler"}
       "1489" {:killed "Javelin"     :by "Prowler"}
       "209"  {:killed "Liberator"   :by "Prowler"}
       "203"  {:killed "Lightning"   :by "Prowler"}
       "204"  {:killed "Magrider"    :by "Prowler"}
       ;"0000" {:killed "Mosquito"    :by "Prowler"}*
       ;"0000" {:killed "Prowler"     :by "Prowler"}*
       "208"  {:killed "Reaver"      :by "Prowler"}
       "207"  {:killed "Scythe"      :by "Prowler"}
       "202"  {:killed "Sunderer"    :by "Prowler"}
       "512"  {:killed "Valkyrie"    :by "Prowler"}
       "205"  {:killed "Vanguard"    :by "Prowler"}

       "154"  {:killed "Player"      :by "Liberator"}
       "661"  {:killed "ANT"         :by "Liberator"}
       ;"0000" {:killed "Chimera"     :by "Liberator"}
       ;"0000" {:killed "Colossus"    :by "Liberator"}
       "1998" {:killed "Corsair"     :by "Liberator"}
       "1643" {:killed "Dervish"     :by "Liberator"}
       "211"  {:killed "Flash"       :by "Liberator"}
       "221"  {:killed "Galaxy"      :by "Liberator"}
       "312"  {:killed "Harasser"    :by "Liberator"}
       "1490" {:killed "Javelin"     :by "Liberator"}
       "220"  {:killed "Liberator"   :by "Liberator"}
       "213"  {:killed "Lightning"   :by "Liberator"}
       "214"  {:killed "Magrider"    :by "Liberator"}
       "219"  {:killed "Mosquito"    :by "Liberator"}
       "216"  {:killed "Prowler"     :by "Liberator"}
       "218"  {:killed "Reaver"      :by "Liberator"}
       "217"  {:killed "Scythe"      :by "Liberator"}
       "212"  {:killed "Sunderer"    :by "Liberator"}
       "513"  {:killed "Valkyrie"    :by "Liberator"}
       "215"  {:killed "Vanguard"    :by "Liberator"}

       "155"  {:killed "Player"      :by "Galaxy"}
       "662"  {:killed "ANT"         :by "Galaxy"}
       ;"0000" {:killed "Chimera"     :by "Galaxy"}
       ;"0000" {:killed "Colossus"    :by "Galaxy"}
       "1999" {:killed "Corsair"     :by "Galaxy"}
       "1644" {:killed "Dervish"     :by "Galaxy"}
       "222"  {:killed "Flash"       :by "Galaxy"}
       "232"  {:killed "Galaxy"      :by "Galaxy"}
       "313"  {:killed "Harasser"    :by "Galaxy"}
       "1491" {:killed "Javelin"     :by "Galaxy"}
       "231"  {:killed "Liberator"   :by "Galaxy"}
       "224"  {:killed "Lightning"   :by "Galaxy"}
       "225"  {:killed "Magrider"    :by "Galaxy"}
       "230"  {:killed "Mosquito"    :by "Galaxy"}
       "227"  {:killed "Prowler"     :by "Galaxy"}
       "229"  {:killed "Reaver"      :by "Galaxy"}
       "228"  {:killed "Scythe"      :by "Galaxy"}
       "223"  {:killed "Sunderer"    :by "Galaxy"}
       "514"  {:killed "Valkyrie"    :by "Galaxy"}
       "226"  {:killed "Vanguard"    :by "Galaxy"}

       "314"  {:killed "Player"      :by "Harasser"}
       "666"  {:killed "ANT"         :by "Harasser"}
       ;"0000" {:killed "Chimera"     :by "Harasser"}
       ;"0000" {:killed "Colossus"    :by "Harasser"}
       "2003" {:killed "Corsair"     :by "Harasser"}
       "1648" {:killed "Dervish"     :by "Harasser"}
       "315"  {:killed "Flash"       :by "Harasser"}
       "323"  {:killed "Galaxy"      :by "Harasser"}
       "324"  {:killed "Harasser"    :by "Harasser"}
       "1494" {:killed "Javelin"     :by "Harasser"}
       "322"  {:killed "Liberator"   :by "Harasser"}
       "317"  {:killed "Lightning"   :by "Harasser"}
       "325"  {:killed "Magrider"    :by "Harasser"}
       "321"  {:killed "Mosquito"    :by "Harasser"}
       "319"  {:killed "Prowler"     :by "Harasser"}
       "320"  {:killed "Reaver"      :by "Harasser"}
       "326"  {:killed "Scythe"      :by "Harasser"}
       "316"  {:killed "Sunderer"    :by "Harasser"}
       "576"  {:killed "Valkyrie"    :by "Harasser"}
       "318"  {:killed "Vanguard"    :by "Harasser"}

       "515"  {:killed "Player"      :by "Valkyrie"}
       "671"  {:killed "ANT"         :by "Valkyrie"}
       ;"0000" {:killed "Chimera"     :by "Valkyrie"}
       ;"0000" {:killed "Colossus"    :by "Valkyrie"}
       "2009" {:killed "Corsair"     :by "Valkyrie"}
       "1654" {:killed "Dervish"     :by "Valkyrie"}
       "520"  {:killed "Flash"       :by "Valkyrie"}
       "528"  {:killed "Galaxy"      :by "Valkyrie"}
       "575"  {:killed "Harasser"    :by "Valkyrie"}
       "1500" {:killed "Javelin"     :by "Valkyrie"}
       "527"  {:killed "Liberator"   :by "Valkyrie"}
       "522"  {:killed "Lightning"   :by "Valkyrie"}
       "529"  {:killed "Magrider"    :by "Valkyrie"}
       "526"  {:killed "Mosquito"    :by "Valkyrie"}
       "524"  {:killed "Prowler"     :by "Valkyrie"}
       "525"  {:killed "Reaver"      :by "Valkyrie"}
       "530"  {:killed "Scythe"      :by "Valkyrie"}
       "521"  {:killed "Sunderer"    :by "Valkyrie"}
       "533"  {:killed "Valkyrie"    :by "Valkyrie"}
       "523"  {:killed "Vanguard"    :by "Valkyrie"}

       "681"  {:killed "Player"      :by "ANT"}
       "657"  {:killed "ANT"         :by "ANT"}
       ;"0000" {:killed "Chimera"     :by "ANT"}
       ;"0000" {:killed "Colossus"    :by "ANT"}
       "2011" {:killed "Corsair"     :by "ANT"}
       "1656" {:killed "Dervish"     :by "ANT"}
       "676"  {:killed "Flash"       :by "ANT"}
       "677"  {:killed "Galaxy"      :by "ANT"}
       "678"  {:killed "Harasser"    :by "ANT"}
       "1501" {:killed "Javelin"     :by "ANT"}
       ;"0000" {:killed "Liberator"   :by "ANT"}
       ;"0000" {:killed "Lightning"   :by "ANT"}
       "679"  {:killed "Magrider"    :by "ANT"}
       "680"  {:killed "Mosquito"    :by "ANT"}
       "682"  {:killed "Prowler"     :by "ANT"}
       "685"  {:killed "Reaver"      :by "ANT"}
       "686"  {:killed "Scythe"      :by "ANT"}
       "688"  {:killed "Sunderer"    :by "ANT"}
       "689"  {:killed "Valkyrie"    :by "ANT"}
       "690"  {:killed "Vanguard"    :by "ANT"}
       
       "1466" {:killed "Player"      :by "Colossus"}
       "1461" {:killed "ANT"         :by "Colossus"}
       ;"0000" {:killed "Chimera"     :by "Colossus"}
       "1475" {:killed "Colossus"    :by "Colossus"}
       "2046" {:killed "Corsair"     :by "Colossus"}
       "1689" {:killed "Dervish"     :by "Colossus"}
       "1459" {:killed "Flash"       :by "Colossus"}
       "1462" {:killed "Galaxy"      :by "Colossus"}
       "1463" {:killed "Harasser"    :by "Colossus"}
       ;"0000" {:killed "Javelin"     :by "Colossus"}
       ;"0000" {:killed "Liberator"   :by "Colossus"}
       ;"0000" {:killed "Lightning"   :by "Colossus"}
       "1464" {:killed "Magrider"    :by "Colossus"}
       "1465" {:killed "Mosquito"    :by "Colossus"}
       "1467" {:killed "Prowler"     :by "Colossus"}
       "1470" {:killed "Reaver"      :by "Colossus"}
       "1471" {:killed "Scythe"      :by "Colossus"}
       "1473" {:killed "Sunderer"    :by "Colossus"}
       "1474" {:killed "Valkyrie"    :by "Colossus"}
       "1460" {:killed "Vanguard"    :by "Colossus"}

       "1510" {:killed "Player"      :by "Bastion"}
       "1503" {:killed "ANT"         :by "Bastion"}
       "1558" {:killed "Chimera"     :by "Bastion"}
       "1504" {:killed "Colossus"    :by "Bastion"}
       "2047" {:killed "Corsair"     :by "Bastion"}
       "1690" {:killed "Dervish"     :by "Bastion"}
       "1505" {:killed "Flash"       :by "Bastion"}
       "1506" {:killed "Galaxy"      :by "Bastion"}
       "1507" {:killed "Harasser"    :by "Bastion"}
       ;"0000" {:killed "Javelin"     :by "Bastion"}
       ;"0000" {:killed "Liberator"   :by "Bastion"}
       ;"0000" {:killed "Lightning"   :by "Bastion"}
       "1508" {:killed "Magrider"    :by "Bastion"}
       "1509" {:killed "Mosquito"    :by "Bastion"}
       "1511" {:killed "Prowler"     :by "Bastion"}
       "1514" {:killed "Reaver"      :by "Bastion"}
       "1515" {:killed "Scythe"      :by "Bastion"}
       "1517" {:killed "Sunderer"    :by "Bastion"}
       "1518" {:killed "Valkyrie"    :by "Bastion"}
       "1519" {:killed "Vanguard"    :by "Bastion"}

       "1572" {:killed "Player"      :by "Chimera"}
       "1587" {:killed "ANT"         :by "Chimera"}
       "1559" {:killed "Chimera"     :by "Chimera"}
       ;"0000" {:killed "Colossus"    :by "Chimera"}
       "2048" {:killed "Corsair"     :by "Chimera"}
       "1691" {:killed "Dervish"     :by "Chimera"}
       "1573" {:killed "Flash"       :by "Chimera"}
       "1581" {:killed "Galaxy"      :by "Chimera"}
       "1582" {:killed "Harasser"    :by "Chimera"}
       ;"0000" {:killed "Javelin"     :by "Chimera"}
       ;"0000" {:killed "Liberator"   :by "Chimera"}
       "1735" {:killed "Lightning"   :by "Chimera"}
       "1574" {:killed "Magrider"    :by "Chimera"}
       "1575" {:killed "Mosquito"    :by "Chimera"}
       "1576" {:killed "Prowler"     :by "Chimera"}
       "1577" {:killed "Reaver"      :by "Chimera"}
       "1578" {:killed "Scythe"      :by "Chimera"}
       "1579" {:killed "Sunderer"    :by "Chimera"}
       "1585" {:killed "Valkyrie"    :by "Chimera"}
       "1580" {:killed "Vanguard"    :by "Chimera"}

       ;"1597" {:killed "Player"      :by "Flash"}
       ;"0000" {:killed "ANT"         :by "Flash"}
       ;"0000" {:killed "Chimera"     :by "Flash"}
       ;"0000" {:killed "Colossus"    :by "Flash"}
       ;"2049" {:killed "Corsair"     :by "Flash"}
       ;"1692" {:killed "Dervish"     :by "Flash"}
       ;"1598" {:killed "Flash"       :by "Flash"}
       ;"1608" {:killed "Galaxy"      :by "Flash"}
       ;"1609" {:killed "Harasser"    :by "Flash"}
       ;"1614" {:killed "Javelin"     :by "Flash"}
       ;"1607" {:killed "Liberator"   :by "Flash"}
       ;"1600" {:killed "Lightning"   :by "Flash"}
       ;"1601" {:killed "Magrider"    :by "Flash"}
       ;"1606" {:killed "Mosquito"    :by "Flash"}
       ;"1603" {:killed "Prowler"     :by "Flash"}
       ;"1605" {:killed "Reaver"      :by "Flash"}
       ;"1604" {:killed "Scythe"      :by "Flash"}
       ;"1599" {:killed "Sunderer"    :by "Flash"}
       ;"1612" {:killed "Valkyrie"    :by "Flash"}
       ;"1602" {:killed "Vanguard"    :by "Flash"}

       ;"1616" {:killed "Player"      :by "Javelin"}
       ;"0000" {:killed "ANT"         :by "Javelin"}
       ;"0000" {:killed "Chimera"     :by "Javelin"}
       ;"0000" {:killed "Colossus"    :by "Javelin"}
       ;"2050" {:killed "Corsair"     :by "Javelin"}
       ;"1693" {:killed "Dervish"     :by "Javelin"}
       ;"1617" {:killed "Flash"       :by "Javelin"}
       ;"1627" {:killed "Galaxy"      :by "Javelin"}
       ;"1628" {:killed "Harasser"    :by "Javelin"}
       ;"1633" {:killed "Javelin"     :by "Javelin"}
       ;"1626" {:killed "Liberator"   :by "Javelin"}
       ;"1619" {:killed "Lightning"   :by "Javelin"}
       ;"1620" {:killed "Magrider"    :by "Javelin"}
       ;"1625" {:killed "Mosquito"    :by "Javelin"}
       ;"1622" {:killed "Prowler"     :by "Javelin"}
       ;"1624" {:killed "Reaver"      :by "Javelin"}
       ;"1623" {:killed "Scythe"      :by "Javelin"}
       ;"1618" {:killed "Sunderer"    :by "Javelin"}
       ;"1631" {:killed "Valkyrie"    :by "Javelin"}
       ;"1621" {:killed "Vanguard"    :by "Javelin"}

       "1704" {:killed "Player"      :by "Dervish"}
       "1695" {:killed "ANT"         :by "Dervish"}
       ;"0000" {:killed "Chimera"     :by "Dervish"}
       ;"0000" {:killed "Colossus"    :by "Dervish"}
       "2051" {:killed "Corsair"     :by "Dervish"}
       "1697" {:killed "Dervish"     :by "Dervish"}
       "1698" {:killed "Dervish"     :by "Dervish"} ; TODO duplicate, find out which one is real?
       "1699" {:killed "Flash"       :by "Dervish"}
       "1700" {:killed "Galaxy"      :by "Dervish"}
       "1701" {:killed "Harasser"    :by "Dervish"}
       ;"0000" {:killed "Javelin"     :by "Dervish"}
       ;"0000" {:killed "Liberator"   :by "Dervish"}
       ;"0000" {:killed "Lightning"   :by "Dervish"}
       "1702" {:killed "Magrider"    :by "Dervish"}
       "1703" {:killed "Mosquito"    :by "Dervish"}
       "1705" {:killed "Prowler"     :by "Dervish"}
       "1708" {:killed "Reaver"      :by "Dervish"}
       "1694" {:killed "Scythe"      :by "Dervish"}
       "1710" {:killed "Sunderer"    :by "Dervish"}
       "1711" {:killed "Valkyrie"    :by "Dervish"}
       "1712" {:killed "Vanguard"    :by "Dervish"}
       
       "2074" {:killed "Player"      :by "Corsair"}
       ;"0000" {:killed "ANT"         :by "Corsair"}
       ;"0000" {:killed "Chimera"     :by "Corsair"}
       ;"0000" {:killed "Colossus"    :by "Corsair"}
       "2055" {:killed "Corsair"     :by "Corsair"}
       "2058" {:killed "Dervish"     :by "Corsair"}
       "2061" {:killed "Flash"       :by "Corsair"}
       "2064" {:killed "Galaxy"      :by "Corsair"}
       ;"0000" {:killed "Harasser"    :by "Corsair"}
       "2067" {:killed "Javelin"     :by "Corsair"}
       ;"0000" {:killed "Liberator"   :by "Corsair"}
       ;"0000" {:killed "Lightning"   :by "Corsair"}
       "2068" {:killed "Magrider"    :by "Corsair"}
       "2071" {:killed "Mosquito"    :by "Corsair"}
       "2077" {:killed "Prowler"     :by "Corsair"}
       "2082" {:killed "Reaver"      :by "Corsair"}
       "2085" {:killed "Scythe"      :by "Corsair"}
       "2089" {:killed "Sunderer"    :by "Corsair"}
       "2092" {:killed "Valkyrie"    :by "Corsair"}
       "2095" {:killed "Vanguard"    :by "Corsair"}
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

(def get-weapons
    (memoize (fn []
                 (let [url    (create-url "item" ["item_type_id" "26"
                                                  "c:show" "item_id,name.en"
                                                  "c:lang" "en"
                                                  "c:limit" "2000"])
                       result (client/get url)
                       body   (helper/read-json (:body result))
                       m      (reduce #(assoc %1 (:item-id %2) (get-in %2 [:name :en])) {} (:item-list body))]
                     (assoc m "0" "Ram/Roadkill/Fall")))))

(defn get-outfit-by-id
    [outfit-id]
    (let [url    (create-url "outfit" ["outfit_id" outfit-id
                                       "c:resolve" "member_character"
                                       "c:join" "type:characters_world^on:members.character_id^to:character_id^inject_at:world"])
          result (client/get url)
          body   (helper/read-json (:body result))]
        (first (:outfit-list body))))
