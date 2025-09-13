(ns jwt-authentication.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [taoensso.carmine :as car :refer [wcar]]))

(def redis-token-set-key "valid-refresh-tokens")


(defn create-user!
  "Creates a new user in the database."
  [db-conn {:keys [username password-hash]}]
  (println {:db-conn db-conn
            :username username
            :password-hash password-hash})
  (sql/insert! db-conn :users {:username username :password_hash password-hash}))

(defn find-user-by-username
  "Retrieves a user from the database by their username."
  [db-conn username]
  (sql/get-by-id db-conn :users username :username {}))

(defn add-token!
  [redis-conn token]
  (wcar redis-conn (car/sadd redis-token-set-key token)))

(defn remove-token!
  "Removes a refresh token from the valid set in Redis."
  [redis-conn token]
  (wcar redis-conn (car/srem redis-token-set-key token)))

(defn token-exists?
  "Checks if a refresh token exists in the valid set in Redis."
  [redis-conn token]
  (= 1 (wcar redis-conn (car/sismember redis-token-set-key token))))