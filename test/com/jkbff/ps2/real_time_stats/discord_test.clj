(ns com.jkbff.ps2.real-time-stats.discord-test
  (:require [clojure.test :refer :all]
            [com.jkbff.ps2.real_time_stats.discord :as discord]))

(defn time-execution
  [f]
  (let [begin-time (System/currentTimeMillis)]
    (f)
    (- (System/currentTimeMillis) begin-time)))

(defn execute-time-test
  [num-messages max-messages recovery-time]
  (let [state (atom {:last-sent-at 0 :messages-left max-messages})]
    (time-execution (fn [] (dotimes [n num-messages]
                             (swap! state discord/wait-for-available-message max-messages recovery-time))))))

(deftest wait-for-available-message-test
  (testing "more messages than available messages should not hit burst limit"
    (let [time-elapsed (execute-time-test 15 30 2)]
      (is (< time-elapsed 1000))))
  (testing "more messages available than messages should hit burst limit"
    (let [time-elapsed (execute-time-test 10 5 1)]
      (is (> time-elapsed 5000))
      (is (< time-elapsed 6000))))
  (testing "recovery time less than 1 should throw AssertionError"
    (is (thrown? java.lang.AssertionError (execute-time-test 10 1 0.0001)))))

