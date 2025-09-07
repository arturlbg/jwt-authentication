(defproject jwt-authentication "1.0.0"
  :description "Clojure JWT Authentication Example"
  :license {:name "MIT"}
  :dependencies [[org.clojure/clojure "1.12.2"]
                 [http-kit "2.8.1"]
                 [metosin/reitit "0.7.0"]
                 [ring/ring-json "0.5.1"]
                 [buddy/buddy-auth "3.0.323"]
                 [buddy/buddy-sign "3.5.346"]
                 [environ "1.2.0"]]
  :plugins [[lein-dotenv "1.0.0"]]
  :main ^:skip-aot jwt-authentication.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
