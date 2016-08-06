(defproject cljstache "2.0.0-SNAPSHOT"
  :min-lein-version "2.5.2"
  :description "{{ mustache }} for Clojure[Script]"
  :url "http://github.com/fhd/clostache"
  :license {:name "GNU Lesser General Public License 2.1"
            :url "http://www.gnu.org/licenses/lgpl-2.1.txt"
            :distribution :repo}
  :jvm-opts ["-Xmx1g"]

  :dependencies []

  :profiles {:dev {:dependencies [[org.clojure/clojure "1.8.0"]
                                  [org.clojure/data.json "0.1.2"]
                                  [jline/jline "0.9.94"]
                                  [org.mozilla/rhino "1.7.7"]]
                   :resource-paths ["test-resources"]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}
             :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}
             :1.9 {:dependencies [[org.clojure/clojure "1.9.0-alpha10"]]}
             :cljs {:dependencies [[org.clojure/clojurescript "1.8.51"]]
                    :plugins [[lein-cljsbuild "1.1.3"]
                              [lein-doo "0.1.6"]]}}

  :aliases {"with-clj" ["with-profile" "dev:dev,1.7:dev,1.8:dev,1.9"]
            "with-cljs" ["with-profile" "cljs"]
            "test-clj" ["with-clj" "test"]
            "test-cljs" ["with-cljs" "doo" "rhino" "test" "once"]
            "test-all" ["do" "clean," "test-clj," "test-cljs"]
            "deploy" ["do" "clean," "deploy" "clojars"]}

  :jar-exclusions [#"\.swp|\.swo|\.DS_Store"]

  :doo {:paths {:rhino "lein run -m org.mozilla.javascript.tools.shell.Main"}}

  :global-vars {*warn-on-reflection* true}

  :cljsbuild {:builds
              {:test {:source-paths ["src" "test"]
                      :compiler {:output-to "target/unit-test.js"
                                 :main 'cljstache.runner
                                 :optimizations :whitespace}}}})
