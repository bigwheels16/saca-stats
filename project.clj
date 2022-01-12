(defproject ps2-real-time-stats "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [stylefruits/gniazdo "1.1.1"]              ; web socket client
                 [org.clojure/data.json "0.2.6"]
                 [com.github.seancorfield/next.jdbc "1.2.761"]
                 [hikari-cp "2.13.0"] ; https://github.com/tomekw/hikari-cp
                 [com.h2database/h2 "2.0.206"]
                 [clj-http "3.7.0"]
                 [ch.qos.logback/logback-classic "1.2.10"]]
  :main com.jkbff.ps2.real-time-stats.core
  :init-ns com.jkbff.ps2.real-time-stats.core
  :repl-options {:init-ns com.jkbff.ps2.real-time-stats.core})
