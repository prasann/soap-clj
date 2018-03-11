(ns soap-clj.core-test
  (:require [clojure.test :refer :all]
            [soap-clj.core :refer :all]))

(deftest find-my-country-int-test
  (is (= "United States" (soap-clj.core/find-country-of "65.20.10.10")))
  (is (= "Japan" (soap-clj.core/find-country-of "43.30.10.25")))
  )
