(ns jwt-authentication.handler.api
  (:require [reitit.ring :as ring]
            [ring.util.response :as response]
            [jwt-authentication.auth :as auth]
            [buddy.auth :refer [authenticated?]]
            [ring.middleware.json :as ring-json]))

(def posts
  [{:username "Kyle", :title "Post 1"}
   {:username "Jim", :title "Post 2"}])

(defn get-posts-handler [req]
  (if-not (authenticated? req)
    (-> (response/response {:error "Unauthorized"})
        (response/status 401))
    (let [identity (:identity req)
          username (get-in identity [:user :name])]
      (response/response
        (filter #(= (:username %) username) posts)))))

(defn api-routes [config]
  (ring/ring-handler
    (ring/router
      [["/posts"
        {:get {:handler get-posts-handler}
         :middleware [[auth/wrap-authentication (:access-token-secret config)]]}]]

      {:data {:middleware [ring-json/wrap-json-response]}})))
