(ns com.jkbff.ps2.real-time-stats.api-test
  (:require [clojure.test :refer :all]
            [com.jkbff.ps2.real_time_stats.api :as api]))

(deftest create-url-test
  (testing "no parameters"
    (is (= "https://census.daybreakgames.com/s:/get/ps2:v2/test?" (api/create-url "test" {}))))
  (testing "single parameter"
    (is (= "https://census.daybreakgames.com/s:/get/ps2:v2/test?a=1" (api/create-url "test" {"a" "1"}))))
  (testing "multiple parameters"
    (is (= "https://census.daybreakgames.com/s:/get/ps2:v2/test?a=1&b=2&c=3" (api/create-url "test" {"a" "1" "b" "2" "c" "3"})))))