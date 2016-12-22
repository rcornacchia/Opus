(ns account-api-v2.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[account-api-v2 started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[account-api-v2 has shut down successfully]=-"))
   :middleware identity})
