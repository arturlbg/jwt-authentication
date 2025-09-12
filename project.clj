(defproject jwt-authentication "1.0.0"
  :description "Clojure JWT Authentication Service Template"
  :license {:name "MIT"}
  :repositories [["central" {:url "https://repo1.maven.org/maven2/"}]
                 ["clojars" {:url "https://repo.clojars.org/"}]]
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [http-kit "2.6.0"]
                 [metosin/reitit "0.7.0-alpha7"]
                 [ring/ring-json "0.5.1"]
                 [buddy/buddy-auth "3.0.323"]
                 [buddy/buddy-sign "3.5.346"]
                 [buddy/buddy-hashers "2.0.167"]
                 [com.github.seancorfield/next.jdbc "1.3.1048"]
                 [org.postgresql/postgresql "42.5.1"]
                 [com.taoensso/carmine "3.2.0"]
                 [environ "1.2.0"]]
  :main ^:skip-aot jwt-authentication.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
