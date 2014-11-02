(require
 '[pallet.crate.mysql-test
   :refer [live-test-spec]]
 '[pallet.crates.test-nodes :refer [node-specs]])

(defproject mysql-crate
  :provider node-specs                  ; supported pallet nodes
  :groups [(group-spec "mysql-live-test"
                       :extends [with-automated-admin-user
                                 live-test-spec]
                       :roles #{:simple-test})
           ])
