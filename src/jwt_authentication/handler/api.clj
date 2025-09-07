(ns jwt-authentication.handler.api
  (:require [reitit.ring :as ring]
            [ring.util.response :as response]
            [jwt-authentication.auth :as auth]
            [ring.middleware.json :as ring-json]))

(def posts
  [{:username "Kyle", :title "Post 1"}
   {:username "Jim", :title "Post 2"}])

(defn get-posts-handler [req]
  (let [identity (:identity req)
        username (get-in identity [:user :name])]
    (response/response
      (filter #(= (:username %) username) posts))))

(defn api-routes [config]
  (ring/ring-handler
    (ring/router
      ["/posts" {:get        {:handler get-posts-handler}
                 :middleware [[auth/wrap-authentication (:access-token-secret config)]
                              [auth/wrap-authorization]]}]
      {:data {:middleware [[ring-json/wrap-json-body {:keywords? true}]
                           ring-json/wrap-json-response]}})))
