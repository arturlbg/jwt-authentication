(ns jwt-authentication.handler.auth
  (:require [reitit.ring :as ring]
            [ring.util.response :as response]
            [jwt-authentication.auth :as auth]
            [jwt-authentication.db :as db]
            [ring.middleware.json :as ring-json]))

(defn auth-routes [{:keys [config db-conn redis-conn]}]
  (ring/ring-handler
    (ring/router
      [["/signup"
        {:post
         (fn [{:keys [body]}]
           (let [username (:username body)
                 password (:password body)]
             (if (or (empty? username) (empty? password))
               (response/bad-request {:error "Username and password are required"})
               (try
                 (db/create-user! db-conn {:username username
                                           :password-hash (auth/hash-password password)})
                 (response/status (response/response {:message "User created"}) 201)
                 (catch org.postgresql.util.PSQLException e
                   (if (.contains (.getMessage e) "duplicate key")
                     (-> (response/response {:error "Username already exists"})
                         (response/status 409))
                     (-> (response/response {:error "Database error"})
                         (response/status 500))))))))}]

       ["/login"
        {:post
         (fn [{:keys [body]}]
           (let [username (:username body)
                 password (:password body)
                 user (db/find-user-by-username db-conn username)]
             (if (and user (auth/check-password password (:password_hash user)))
               (let [user-claims {:name (:username user) :id (:id user)}
                     access-token (auth/generate-access-token user-claims (:access-token-secret config))
                     refresh-token (auth/generate-refresh-token user-claims (:refresh-token-secret config))]
                 (db/add-token! redis-conn refresh-token)
                 (response/response {:accessToken access-token
                                     :refreshToken refresh-token}))
               (-> (response/response {:error "Invalid username or password"})
                   (response/status 401)))))}]

       ["/token"
        {:post
         (fn [{:keys [body]}]
           (let [refresh-token (:token body)]
             (cond
               (nil? refresh-token)
               (-> (response/response {:error "Token not provided"})
                   (response/status 401))

               (not (db/token-exists? redis-conn refresh-token))
               (-> (response/response {:error "Invalid refresh token"})
                   (response/status 403))

               :else
               (if-let [claims (auth/verify-token refresh-token (:refresh-token-secret config))]
                 (let [user (:user claims)
                       new-access-token (auth/generate-access-token user (:access-token-secret config))]
                   (response/response {:accessToken new-access-token}))
                 (-> (response/response {:error "Refresh token verification failed"})
                     (response/status 403))))))}]

       ["/logout"
        {:delete
         (fn [{:keys [body]}]
           (when-let [token (:token body)]
             (db/remove-token! redis-conn token))
           (response/status 204))}]]

      {:data {:middleware [(ring-json/wrap-json-body {:keywords? true})
                           ring-json/wrap-json-response]}})))