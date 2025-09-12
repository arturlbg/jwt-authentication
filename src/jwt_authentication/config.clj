(ns jwt-authentication.config
  (:require [environ.core :refer [env]]))

(defn load-config []
  {:access-token-secret  (or (:access-token-secret env)
                             (throw (ex-info "ACCESS_TOKEN_SECRET not set" {})))
   :refresh-token-secret (or (:refresh-token-secret env)
                             (throw (ex-info "REFRESH_TOKEN_SECRET not set" {})))
   :database-url         (or (:database-url env)
                             (throw (ex-info "DATABASE_URL not set" {})))
   :redis-url            (or (:redis-url env)
                             (throw (ex-info "REDIS_URL not set" {})))})