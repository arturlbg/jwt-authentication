(ns jwt-authentication.db)

(defonce refresh-tokens (atom #{}))

(defn add-token! [token]
  (swap! refresh-tokens conj token))

(defn remove-token! [token]
  (swap! refresh-tokens disj token))

(defn token-exists? [token]
  (contains? @refresh-tokens token))
