{:dev
 {:dependencies [[com.palletops/pallet "0.8.0-RC.10" :classifier "tests"]
                 [com.palletops/crates "0.1.1"]
                 [com.palletops/pallet-vmfest "0.3.0-beta.2"]
                 [ch.qos.logback/logback-classic "1.0.9"]]
  :plugins [[com.palletops/pallet-lein "0.8.0-alpha.1"]
            [com.palletops/lein-pallet-crate "0.1.0"]
            [lein-pallet-release "0.1.3"]
            [lein-resource "0.3.2"]
            [codox/codox.leiningen "0.6.4"]
            [lein-marginalia "0.7.1"]]
  :pallet-release
  {:url "https://pbors:${GH_TOKEN}@github.com/pallet/mysql-crate.git"
   :branch "master"}
  :aliases {"live-test-up"
            ["pallet" "up" "--phases" "install,configure,test"]
            "live-test-down" ["pallet" "down"]
            "live-test" ["do" "live-test-up," "live-test-down"]
            "replica-test-up"
            ["pallet" "up"
             "--phases" "install,configure,restart,init-replica-set,test"
             "--roles" "replica-test"]
            "replica-test-down"
            ["pallet" "down" "--roles" "replica-test"]}
  :test-selectors {:default (complement :live-test)
                   :live-test :live-test
                   :all (constantly true)}}
 :no-checkouts {:checkout-deps-shares ^{:replace true} []}
 :doc {:dependencies [[com.palletops/pallet-codox "0.1.0"]]
       :plugins [[codox/codox.leiningen "0.6.4"]
                 [lein-marginalia "0.7.1"]]
       :codox {:writer codox-md.writer/write-docs
               :output-dir "doc/0.8/api"
               :src-dir-uri
               "https://github.com/pallet/mysql-crate/blob/develop"
               :src-linenum-anchor-prefix "L"}
       :aliases {"marg" ["marg" "-d" "doc/0.8/annotated"]
                 "codox" ["doc"]
                 "doc" ["do" "codox," "marg"]}
       }
 :release
 {:set-version
  {:updates [{:path "README.md" :no-snapshot true}]}}}
