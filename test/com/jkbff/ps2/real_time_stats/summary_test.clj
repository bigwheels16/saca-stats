(ns com.jkbff.ps2.real-time-stats.summary-test
  (:require [clojure.test :refer :all]
            [com.jkbff.ps2.real_time_stats.summary :as summary]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 0))))

(deftest get-char-stats-sorted-test
  (testing "data"
    (let [data [{:experience-id "1" :amount "10"}
                {:experience-id "2" :amount "20"}
                {:experience-id "3" :amount "30"}
                {:experience-id "1" :amount "10"}
                {:experience-id "2" :amount "20"}
                {:experience-id "1" :amount "10"}
                {:experience-id "2" :amount "20"}
                {:experience-id "1" :amount "10"}]
          sorted (summary/get-char-stats-sorted data)]
      (println sorted)
      )))