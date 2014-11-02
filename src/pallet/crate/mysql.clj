(ns pallet.crate.mysql
  (:require
   [pallet.action :as action]
   [pallet.actions :as actions]
   [pallet.crate :refer [assoc-settings defplan get-settings]]
   [pallet.stevedore :as stevedore]
   [pallet.template :as template]
   [clojure.string :as string]
   [simple-ini.core :as simple-ini]
   [clojure.tools.logging :as log :refer [debugf]]
   [pallet.crate-install :as crate-install]
   [pallet.actions :refer [remote-file package]]
   [pallet.version-dispatch
    :refer [defmethod-version-plan defmulti-version-plan]])
  (:use
   pallet.thread-expr))

(def facility :mysql)

(def default-my-cnf
  {
   :mysqld
   {
    :datadir "/var/lib/mysql"
    :socket "/var/lib/mysql/mysql.sock"
    :user "mysql"
    :symbolic-links 0
    }
   :mysqld_safe
   {
    :log-error "/var/log/mysqld.log"
    :pid-file "/var/run/mysqld/mysqld.pid"
    }
   })


(defn default-settings
  "Provides default settings, that are merged with any user supplied settings."
  []
  ;; TODO add configuration options here
  {})

;; use settings-map pattern as per runit-crate
;; allows us to apply *dynamic* plaftorm defaults

(defmulti-version-plan settings-map [version settings])

(defmethod-version-plan settings-map {:os :ubuntu}
   [os os-version version settings]
  (let [{:keys [root-password start-on-boot] :or {start-on-boot false}} settings]
  {
   :config default-my-cnf
   :install-strategy :packages
   :packages ["mysql-server"]
   :preseeds [{:line (str "mysql-server-5.5 mysql-server/root_password password " root-password)}
              {:line (str "mysql-server-5.5 mysql-server/root_password_again password " root-password)}
              {:line (str "mysql-server-5.5 mysql-server/start_on_boot boolean " start-on-boot)}
              {:line (str "mysql-server-5.6 mysql-server/root_password password " root-password)}
              {:line (str "mysql-server-5.6 mysql-server/root_password_again password " root-password)}
              {:line (str "mysql-server-5.6 mysql-server/start_on_boot boolean " start-on-boot)}
              ]
   :conf-file "/etc/mysql/my.cnf"
  }))


(defmethod-version-plan settings-map {:os :rh-base}
   [os os-version version settings]
  {
   :config default-my-cnf
   :install-strategy :packages
   :packages ["mysql-server"]
   :conf-file "/etc/my.cnf"
  })


(defplan settings
  "Settings for mysql"
  [{:keys [instance-id] :as settings}
   {:keys [instance-id] :as options}]
  (let [settings (merge (default-settings) settings)
        settings (settings-map (:version settings) settings)]
    (assoc-settings facility settings {:instance-id instance-id})))


;;; # Install
(defplan install
  "Install mysql"
  [{:keys [instance-id]}]
    (crate-install/install facility instance-id))


;;   (let [settings (get-settings :mysql {:instance-id instance-id})]
;;     (debugf "Install mysql settings %s" settings)

(defplan configure
  "Configure mysql"
  [{:keys [instance-id] :as options}]
  (let [{:keys [conf-file config]}
        (get-settings facility {:instance-id instance-id})]
    (remote-file conf-file
                 :content (simple-ini/serialize config)
                 )))


;; (defn- mysql-script*
;;   "MYSQL script invocation"
;;   [username password sql-script]
;;   (stevedore/script
;;    ("{\n" mysql "-u" ~username ~(str "--password=" password)
;;     ~(str "<<EOF\n" (string/replace sql-script "`" "\\`") "\nEOF\n}"))))

;; (def ^{:private true}
;;   sql-create-user "GRANT USAGE ON *.* TO %s IDENTIFIED BY '%s'")

;; (defplan mysql-server
;;   "Install mysql server from packages"
;;   [root-password & {:keys [start-on-boot] :or {start-on-boot true}}]

;;    (actions/package-manager
;;     :debconf
;;     (str "mysql-server-5.1 mysql-server/root_password password " root-password)
;;     (str "mysql-server-5.1 mysql-server/root_password_again password " root-password)
;;     (str "mysql-server-5.1 mysql-server/start_on_boot boolean " start-on-boot)
;;     (str "mysql-server-5.1 mysql-server-5.1/root_password password " root-password)
;;     (str "mysql-server-5.1 mysql-server-5.1/root_password_again password " root-password)
;;     (str "mysql-server-5.1 mysql-server/start_on_boot boolean " start-on-boot))
;;    (package/package "mysql-server")
;;    (when->
;;     (= :yum (session/packager))
;;     (when->
;;      start-on-boot
;;      (actions/service "mysqld" :action :enable))
;;     (actions/service "mysqld" :action :start)
;;     (actions/exec-checked-script
;;      "Set Root Password"
;;      (chain-or
;;       ("/usr/bin/mysqladmin" -u root password (quoted ~root-password))
;;       (echo "Root password already set"))))


;;      (let [settings (get-settings facility {:instance-id instance-id})
;;            settings (update-in settings [:root-password] root-password)]
;;      (assoc-settings facility settings {:instance-id instance-id}))
;;    )

;;(assoc-in [:parameters :mysql :root-password] root-password)

;; (template/deftemplate my-cnf-template
;;   [string]
;;   {{:path (mysql-my-cnf (session/packager))
;;     :owner "root" :mode "0440"}
;;    string})

;; (action/def-bash-action mysql-conf
;;   "my.cnf configuration file for mysql"
;;   [config]
;;   (template/apply-templates #(my-cnf-template %) [config]))

;; (defn mysql-script
;;   "Execute a mysql script"
;;   [username password sql-script]
;;   (exec-script/exec-checked-script
;;    "MYSQL command"
;;    ~(mysql-script* username password sql-script)))

;; (defn create-database
;;   ([session name]
;;      (let [{:keys [root-password]}
;;         (get-settings facility {:instance-id instance-id})]
;;        (create-database
;;           session name "root" root-password)))
;;   ([session name username root-password]
;;      (mysql-script
;;       session
;;       username root-password
;;       (format "CREATE DATABASE IF NOT EXISTS `%s`" name))))

;; (defn create-user
;;   ([session user password]
;;      (let [{:keys [root-password]} (get-settings facility {:instance-id instance-id})]
;;      (create-user
;;       session user password "root" root-password)))
;;   ([session user password username root-password]
;;      (mysql-script
;;       session
;;       username root-password
;;       (format sql-create-user user password))))

;; (defn grant
;;   ([session privileges level user]
;;      (grant
;;       session privileges level user "root"
;;       (parameter/get-for session [:mysql :root-password])))
;;   ([session privileges level user username root-password]
;;      (mysql-script
;;       session
;;       username root-password
;;       (format "GRANT %s ON %s TO %s" privileges level user))))
