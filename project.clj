(defproject scott.jgroups.logger "0.1.0"
  :description "JGroups Custom logger for clojure tools.logging"
  :url "https://github.com/scotthaleen/jgroups-logger"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/tools.logging "0.4.1"]
                 [org.jgroups/jgroups "4.0.15.Final"]]
  :plugins [[lein-shell "0.5.0"]
            [lein-ancient "0.6.15"]
            [lein-changelog "0.3.2"]]
  :aot :all
  :profiles {:dev {:dependencies [[log4j/log4j "1.2.17"]
                                  [org.slf4j/slf4j-log4j12 "1.7.25"]]}
             :provided [:project/provided]
             :lint [:project/provided :project/lint]
             :project/provided {:dependencies [[org.clojure/clojure "1.10.0"]]}
             :project/lint {:plugins [[jonase/eastwood "0.3.4"]]}}
  :aliases {"lint" ["with-profile" "lint" "eastwood"]
            "travis-ci" ["do" ["clean"] ["test"] ["jar"]]})

