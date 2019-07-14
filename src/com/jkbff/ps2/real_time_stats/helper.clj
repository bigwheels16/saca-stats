(ns com.jkbff.ps2.real_time_stats.helper
	(:require [clojure.data.json :as json]))

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

(defn log
	[& args]
	(locking *out*
		(apply println args)))