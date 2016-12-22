(ns user
  (:require [mount.core :as mount]
            account-api-v2.core))

(defn start []
  (mount/start-without #'account-api-v2.core/http-server
                       #'account-api-v2.core/repl-server))

(defn stop []
  (mount/stop-except #'account-api-v2.core/http-server
                     #'account-api-v2.core/repl-server))

(defn restart []
  (stop)
  (start))


