(ns cljstache.runner
  "A stub namespace to run cljs tests using doo"
  (:require [doo.runner :refer-macros [doo-tests]]
            [cljstache.test-parser]
            [cljstache.test-specs]))

(doo-tests 'cljstache.test-parser
           'cljstache.test-specs)
