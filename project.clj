(defproject ps2-real-time-stats "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [stylefruits/gniazdo "1.1.1"]              ; web socket client
                 [org.clojure/data.json "0.2.6"]
                 [clj-http "3.7.0"]]
  :main com.jkbff.ps2.real_time_stats.core
  :repl-options {:init-ns com.jkbff.ps2.real_time_stats.core})
