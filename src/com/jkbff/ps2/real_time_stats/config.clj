(ns com.jkbff.ps2.real_time_stats.config
	(:require [clojure.string :as str]))

(defn get-env-string
	[name]
	(System/getenv name))

(defn get-env-int
	[name]
	(Integer/parseInt (get-env-string name)))

(defn SERVICE_ID [] (get-env-string "SERVICE_ID"))
(defn SUBSCRIBE_CHARACTERS [] (str/split (get-env-string "SUBSCRIBE_CHARACTERS") #","))
(defn DISCORD_WEBHOOK_URL [] (get-env-string "DISCORD_WEBHOOK_URL"))