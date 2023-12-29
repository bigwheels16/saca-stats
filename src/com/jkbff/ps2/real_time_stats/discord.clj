(ns com.jkbff.ps2.real-time-stats.discord
    (:require [com.jkbff.ps2.real-time-stats.config :as config]
              [com.jkbff.helper :as helper]
              [clojure.tools.logging :as log]
              [clj-http.client :as client]))

(def DISCORD_MAX_MESSAGES 15)
(def DISCORD_MESSAGE_RECOVERY_TIME 2)
(def discord-message-state (atom {:last-sent-at 0 :messages-left DISCORD_MAX_MESSAGES}))

(def white-color (* 256 256 256))

(defn get-color-code
    []
    (rand-int (inc white-color)))

(defn wait-for-available-message
    "Prevents sending messages to the Discord API too fast"
    [state max-count recovery-time]

    (assert (>= recovery-time 1))
    (loop []
        (let [current-time (quot (System/currentTimeMillis) 1000)
              new-messages (quot (- current-time (:last-sent-at state)) recovery-time)
              messages-left (min (+ (:messages-left state) new-messages) max-count)]

            (if (< messages-left 1)
                (do
                    (Thread/sleep 1000)
                    (recur))

                (assoc state :last-sent-at (quot (System/currentTimeMillis) 1000)
                             :messages-left (dec messages-left))))))

(defn send-message
    [title description fields]
    (let [obj  {:embeds [{:title       title
                          :type        "rich"
                          :description description
                          :color       (get-color-code)
                          :fields      (filter #(not (clojure.string/blank? (:value %))) fields)}]}
          json (helper/write-json obj)]

        (swap! discord-message-state wait-for-available-message DISCORD_MAX_MESSAGES DISCORD_MESSAGE_RECOVERY_TIME)
        (log/info (str "sending payload to discord with size '" (count json) "'"))

        (client/post (config/DISCORD_WEBHOOK_URL) {:body    json
                                                   :headers {"Content-Type" "application/json"}})))
