(defproject com.palletops/mysql-crate "0.8.0-SNAPSHOT"
  :description "Pallet crate to install, configure and use mysql"
  :url "http://palletops.com/pallet"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [com.palletops/pallet "0.8.0-RC.10"]
                 [io.github.zenlambda.clojure/simple-ini "0.1.0-SNAPSHOT"]
                 ]
  :resource {:resource-paths ["doc-src"]
             :target-path "target/classes/pallet_crate/mysql_crate/"
             :includes [#"doc-src/USAGE.*"]}
  :prep-tasks ["resource" "crate-doc"])
