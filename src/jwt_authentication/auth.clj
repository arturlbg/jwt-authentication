(ns jwt-authentication.auth
  (:require [buddy.sign.jwt :as jwt]
            [buddy.auth.backends :as backends]
            [buddy.auth :refer [authenticated?]]
            [ring.util.response :as response]
            [buddy.auth.middleware :as middleware])
  (:import (java.time Instant)))

(defn generate-access-token [user secret]
  (let [claims {:user user
                :exp  (-> (Instant/now)
                          (.plusSeconds 15)
                          .getEpochSecond)}]
    (jwt/sign claims secret)))

(defn generate-refresh-token [user secret]
  (let [claims {:user user}] ; No expiry for refresh token in this example
    (jwt/sign claims secret)))


(defn verify-token [token secret]
  (try
    (jwt/unsign token secret)
    (catch Exception _ nil)))

(defn auth-backend [secret]
  (backends/jws {:secret secret
                 :token-name "Bearer"}))

(defn wrap-authentication [handler secret]
  (middleware/wrap-authentication handler (auth-backend secret)))

(defn wrap-authorization [handler]
  (fn [req]
    (println "Identity:" (:identity req))
    (if (authenticated? req)
      (handler req)
      (-> (response/response {:error "Unauthorized"})
          (response/status 401)))))

