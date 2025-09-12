(ns jwt-authentication.auth
  (:require [buddy.sign.jwt :as jwt]
            [buddy.auth.backends :as backends]
            [buddy.auth :refer [authenticated?]]
            [ring.util.response :as response]
            [buddy.auth.middleware :as middleware]
            [buddy.hashers :as hashers])
  (:import (java.time Instant)))

(defn hash-password
  "Hashes a password using bcrypt."
  [password]
  (hashers/encrypt password))

(defn check-password
  "Checks a plaintext password against a stored hash."
  [password stored-hash]
  (hashers/check password stored-hash))

(defn generate-access-token [user secret]
  (let [claims {:user user
                :exp  (-> (Instant/now)
                          (.plusSeconds 15)
                          .getEpochSecond)}]
    (jwt/sign claims secret)))

(defn generate-refresh-token [user secret]
  (let [claims {:user user}]
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
    (if (authenticated? req)
      (handler req)
      (-> (response/response {:error "Unauthorized"})
          (response/status 401)))))

