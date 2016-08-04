(defproject cljstache "1.6.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :description "{{ mustache }} for Clojure[Script]"
  :url "http://github.com/fhd/clostache"
  :license {:name "GNU Lesser General Public License 2.1"
            :url "http://www.gnu.org/licenses/lgpl-2.1.txt"
            :distribution :repo}
  :dependencies []
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.8.0"]
                                  [org.clojure/data.json "0.1.2"]
                                  [jline/jline "0.9.94"]]
                   :resource-paths ["test-resources"]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}
             :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}
             :1.9 {:dependencies [[org.clojure/clojure "1.9.0-alpha10"]]}}
  :aliases {"all" ["with-profile" "dev:dev,1.7:dev,1.8:dev,1.9"]}
  :global-vars {*warn-on-reflection* true})
