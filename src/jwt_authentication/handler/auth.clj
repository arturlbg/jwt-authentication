(ns jwt-authentication.handler.auth
  (:require [reitit.ring :as ring]
            [ring.util.response :as response]
            [ring.middleware.json :as json]
            [jwt-authentication.auth :as auth]
            [jwt-authentication.db :as db]))

(defn signup-handler [{:keys [config db-conn redis-conn]}]
  (fn [request]
    (let [body (:body request)
          username (:username body)
          password (:password body)]
      (if (or (nil? username) (nil? password) (empty? username) (empty? password))
        (-> (response/response {:error "Username and password are required"})
            (response/status 400))
        (try
          (db/create-user! db-conn {:username username
                                    :password-hash (auth/hash-password password)})
          (-> (response/response {:message "User created successfully"})
              (response/status 201))
          (catch Exception e
            (if (.contains (.getMessage e) "duplicate key")
              (-> (response/response {:error "Username already exists"})
                  (response/status 409))
              (-> (response/response {:error "Database error"})
                  (response/status 500)))))))))

(defn login-handler [{:keys [config db-conn redis-conn]}]
  (fn [request]
    (let [body (:body request)
          username (:username body)
          password (:password body)]
      (if (or (nil? username) (nil? password) (empty? username) (empty? password))
        (-> (response/response {:error "Username and password are required"})
            (response/status 400))
        (try
          (if-let [user (db/find-user-by-username db-conn username)]
            (if (auth/check-password password (:password_hash user))
              (let [user-claims {:name (:username user) :id (:id user)}
                    access-token (auth/generate-access-token user-claims (:access-token-secret config))
                    refresh-token (auth/generate-refresh-token user-claims (:refresh-token-secret config))]
                (db/add-token! redis-conn refresh-token)
                (-> (response/response {:accessToken access-token
                                        :refreshToken refresh-token
                                        :message "Login successful"})
                    (response/status 200)))
              (-> (response/response {:error "Invalid username or password"})
                  (response/status 401)))
            (-> (response/response {:error "Invalid username or password"})
                (response/status 401)))
          (catch Exception _
            (-> (response/response {:error "Login failed"})
                (response/status 500))))))))

(defn token-handler [{:keys [config db-conn redis-conn]}]
  (fn [request]
    (let [body (:body request)
          refresh-token (:token body)]
      (cond
        (or (nil? refresh-token) (empty? refresh-token))
        (-> (response/response {:error "Refresh token not provided"})
            (response/status 401))

        (not (db/token-exists? redis-conn refresh-token))
        (-> (response/response {:error "Invalid or expired refresh token"})
            (response/status 403))

        :else
        (try
          (if-let [claims (auth/verify-token refresh-token (:refresh-token-secret config))]
            (let [user-claims {:name (:name claims) :id (:id claims)}
                  new-access-token (auth/generate-access-token user-claims (:access-token-secret config))]
              (-> (response/response {:accessToken new-access-token
                                      :message "Token refreshed successfully"})
                  (response/status 200)))
            (-> (response/response {:error "Invalid refresh token"})
                (response/status 403)))
          (catch Exception _
            (-> (response/response {:error "Token refresh failed"})
                (response/status 500))))))))

(defn logout-handler [{:keys [config db-conn redis-conn]}]
  (fn [request]
    (try
      (when-let [token (-> request :body :token)]
        (db/remove-token! redis-conn token))
      (-> (response/response {:message "Logged out successfully"})
          (response/status 200))
      (catch Exception _
        (-> (response/response {:error "Logout failed"})
            (response/status 500))))))

(defn wrap-json [handler]
  (-> handler
      (json/wrap-json-body {:keywords? true})
      json/wrap-json-response))

(defn auth-routes [deps]
  (-> (ring/ring-handler
        (ring/router
          [["/api/auth"
            [["/signup" {:post (signup-handler deps)}]
             ["/login" {:post (login-handler deps)}]
             ["/token" {:post (token-handler deps)}]
             ["/logout" {:post (logout-handler deps)}]]]]
          {})

        (ring/create-default-handler
          {:not-found (fn [request]
                        (-> (response/response {:error "Route not found"
                                                :uri (:uri request)
                                                :method (name (:request-method request))
                                                :available-routes ["/api/auth/signup"
                                                                   "/api/auth/login"
                                                                   "/api/auth/token"
                                                                   "/api/auth/logout"]})
                            (response/status 404)))
           :method-not-allowed (fn [_]
                                 (-> (response/response {:error "Method not allowed"})
                                     (response/status 405)))
           :not-acceptable (fn [_]
                             (-> (response/response {:error "Not acceptable"})
                                 (response/status 406)))}))
      wrap-json))
