(ns com.jkbff.helper
	(:require [clojure.data.json :as json]))

(def time-units [{:unit "hr" :amount 3600} {:unit "min" :amount 60} {:unit "sec" :amount 1}])

(defn entities-fn
	[e]
	(.replace e \- \_))

(defn identifiers-fn
	[e]
	(.replace e \_ \-))

(defn write-json
	[msg]
	(json/write-str msg :key-fn #(entities-fn (name %))))

(defn read-json
	[msg]
	(json/read-str msg :key-fn #(keyword (identifiers-fn %))))

(defn deep-merge [a & maps]
    (if (map? a)
        (apply merge-with deep-merge a maps)
        (apply merge-with deep-merge maps)))

(defn get-time-str
    [timestamp]
    (loop [units-left time-units
           units-arr  []
           time-left  timestamp]

        (if (zero? time-left)
            (clojure.string/join " " units-arr)

            (let [next-unit (first units-left)
                  amount    (quot time-left (:amount next-unit))
                  remainder (rem time-left (:amount next-unit))]

                (if (zero? amount)
                    (recur (rest units-left) units-arr time-left)
                    (recur (rest units-left) (conj units-arr (str amount " " (:unit next-unit))) remainder))))))

; taken from: https://stackoverflow.com/questions/21404130/periodically-calling-a-function-in-clojure
(defn callback-interval [callback ms]
	(future
		(while true
			(try
				(do
					(Thread/sleep ms)
					(callback))
				(catch Exception e (.printStackTrace e))))))

(defn int-to-string
	[i]
	(if (int? i)
		(str i)
		i))

(defn parse-int-values
	[m]
	; parse traverse a map and parse string values into ints where possible
	)