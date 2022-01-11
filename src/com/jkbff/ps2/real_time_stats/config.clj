(ns com.jkbff.ps2.real-time-stats.config
	(:require [clojure.string :as str]))

(defn get-env-string
	[name]
	(clojure.string/trim (or (System/getenv name) "")))

(defn get-env-int
	[name]
	(Integer/parseInt (get-env-string name)))

(defn get-env-bool
	[name]
	(case (.toLowerCase (get-env-string name))
		"0" false
		"false" false
		"1" true
		"true" true))

(defn split-str
	[s]
	(if (.isEmpty s)
		[]
		(str/split s #",")))

(defn SERVICE_ID [] (get-env-string "SERVICE_ID"))
(defn SUBSCRIBE_CHARACTERS [] (split-str (get-env-string "SUBSCRIBE_CHARACTERS")))
(defn SUBSCRIBE_OUTFITS [] (split-str (get-env-string "SUBSCRIBE_OUTFITS")))
(defn SUBSCRIBE_SERVERS [] (split-str (get-env-string "SUBSCRIBE_SERVERS")))
(defn DISCORD_WEBHOOK_URL [] (get-env-string "DISCORD_WEBHOOK_URL"))
(defn IS_DEV [] (get-env-bool "IS_DEV"))
