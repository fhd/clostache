(ns cljstache.runner
  "A stub namespace to run cljs tests using doo"
  (:require [doo.runner :refer-macros [doo-tests]]
            [cljstache.core-test]
            [cljstache.mustache-spec-test]))

(doo-tests 'cljstache.core-test
           'cljstache.mustache-spec-test)
