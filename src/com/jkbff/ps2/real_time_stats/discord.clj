(ns com.jkbff.ps2.real_time_stats.discord
    (:require [com.jkbff.ps2.real_time_stats.config :as config]
              [com.jkbff.ps2.real_time_stats.helper :as helper]
              [clj-http.client :as client]))

(def white-color (* 256 256 256))

(defn get-color-code
    []
    (rand-int (inc white-color)))

(defn send-message
    [title description fields]
    (let [obj  {:embeds [{:title       title
                          :type        "rich"
                          :description description
                          :color       (get-color-code)
                          :fields      (filter #(not (clojure.string/blank? (:value %))) fields)}]}
          json (helper/write-json obj)]

        (client/post (config/DISCORD_WEBHOOK_URL) {:body    json
                                                   :headers {"Content-Type" "application/json"}})))
