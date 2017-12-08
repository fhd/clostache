(defproject de.ubercode.clostache/clostache "1.5.0"
  :min-lein-version "2.0.0"
  :description "{{ mustache }} for Clojure"
  :url "http://github.com/fhd/clostache"
  :license {:name "GNU Lesser General Public License 2.1"
            :url "http://www.gnu.org/licenses/lgpl-2.1.txt"
            :distribution :repo}
  :dependencies [[org.clojure/clojure "1.9.0"]]
  :profiles {:dev {:dependencies [[org.clojure/data.json "0.1.2"]
                                  [jline/jline "0.9.94"]]
                   :resource-paths ["test-resources"]}
             :1.5 {:dependencies [[org.clojure/clojure "1.9.0"]]}}
  :repositories {"clojure-releases" "http://build.clojure.org/releases"}
  :aliases {"all" ["with-profile" "dev:dev,1.5"]}
  :global-vars {*warn-on-reflection* true})
