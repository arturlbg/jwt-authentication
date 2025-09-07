(ns jwt-authentication.handler.auth
    (:require [reitit.ring :as ring]
      [ring.util.response :as response]
      [jwt-authentication.auth :as auth]
      [jwt-authentication.db :as db]
      [ring.middleware.json :as ring-json]))

(defn auth-routes [config]
      (ring/ring-handler
        (ring/router
          [["/login"
            {:post
             (fn [req]
                 (let [username (get-in req [:body :username])]
                   (if (clojure.string/blank? username)
                     (response/bad-request {:error "Username is required"})
                     (let [user {:name username}
                           access-token (auth/generate-access-token user (:access-token-secret config))
                           refresh-token (auth/generate-refresh-token user (:refresh-token-secret config))]
                       (db/add-token! refresh-token)
                       (response/response {:accessToken access-token
                                           :refreshToken refresh-token})))))}]
           ["/token"
            {:post (fn [req]
                       (let [refresh-token (get-in req [:body :token])]
                            (cond
                              (nil? refresh-token)
                              (-> (response/response {:error "Token not provided"})
                                  (response/status 401))

                              (not (db/token-exists? refresh-token))
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
            {:delete (fn [req]
                         (if-let [token (get-in req [:body :token])]
                                 (do
                                   (db/remove-token! token)
                                   (-> (response/response nil)
                                       (response/status 204)))
                                 (-> (response/response {:error "Token not provided"})
                                     (response/status 400))))}]]
          {:data {:middleware [[ring-json/wrap-json-body {:keywords? true}]
                               ring-json/wrap-json-response]}})))
