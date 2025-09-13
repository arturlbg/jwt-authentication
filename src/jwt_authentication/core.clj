(ns jwt-authentication.core
  (:require [org.httpkit.server :as http-kit]
            [next.jdbc :as jdbc]
            [jwt-authentication.config :as config]
            [jwt-authentication.handler.auth :as handler.auth]
            [jwt-authentication.handler.api :as handler.api])
  (:gen-class))

(defn -main [& args]
  (try
    (println "Starting JWT Authentication Service...")
    (let [config (config/load-config)]
      (println "Configuration loaded successfully")
      (let [db-conn (jdbc/get-datasource (:database-url config))]
        (println "Database connection established")
        (let [redis-conn {:pool {} :spec {:uri (:redis-url config)}}]
          (println "Redis connection configured")
          (let [auth-handler (handler.auth/auth-routes {:config     config
                                                        :db-conn    db-conn
                                                        :redis-conn redis-conn})
                api-handler (if (resolve 'handler.api/api-routes)
                              (handler.api/api-routes config)
                              (fn [_] {:status 200 :body "API service not implemented yet"}))]
            (let [auth-server (http-kit/run-server auth-handler {:port 4000})
                  api-server (http-kit/run-server api-handler {:port 3000})]

              (println "auth on http://localhost:4000")
              (println "api on http://localhost:3000")
              (.addShutdownHook
                (Runtime/getRuntime)
                (Thread.
                  (fn []
                    (auth-server :timeout 100)
                    (api-server :timeout 100)
                    (println "shutdown complete"))))
              @(promise))))))

    (catch Exception e
      (println "========= failed to start service ========" (.getMessage e))
      (.printStackTrace e)
      (System/exit 1))))