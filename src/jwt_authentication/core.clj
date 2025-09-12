(ns jwt-authentication.core
  (:require [jwt-authentication.config :as config]
            [jwt-authentication.handler.api :as handler.api]
            [jwt-authentication.handler.auth :as handler.auth]
            [org.httpkit.server :as http-kit]
            [next.jdbc :as jdbc]
            [taoensso.carmine :as car])
  (:gen-class))

(defn -main [& args]
  (let [config (config/load-config)
        db-conn (jdbc/get-datasource (:database-url config))
        redis-conn {:pool {} :spec {:uri (:redis-url config)}}
        api-handler (handler.api/api-routes config)
        auth-handler (handler.auth/auth-routes {:config     config
                                                :db-conn    db-conn
                                                :redis-conn redis-conn})
        api-server (http-kit/run-server api-handler {:port 3000})
        auth-server (http-kit/run-server auth-handler {:port 4000})]
    (println "System started. API on port 3000, Auth on port 4000.")
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. (fn []
                                 (println "Shutting down...")
                                 (api-server)
                                 (auth-server)
                                 (println "Shutdown complete."))))))