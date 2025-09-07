(ns jwt-authentication.config
  (:require [environ.core :refer [env]]))

(defn load-config []
  {:access-token-secret  (or (env :access-token-secret)
                             (throw (ex-info "ACCESS_TOKEN_SECRET not set" {})))
   :refresh-token-secret (or (env :refresh-token-secret)
                             (throw (ex-info "REFRESH_TOKEN_SECRET not set" {})))})
