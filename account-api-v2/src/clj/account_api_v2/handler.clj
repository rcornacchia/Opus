(ns account-api-v2.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [account-api-v2.layout :refer [error-page]]
            [account-api-v2.routes.services :refer [service-routes]]
            [account-api-v2.routes.services :refer [restricted-service-routes]]
            [compojure.route :as route]
            [account-api-v2.env :refer [defaults]]
            [mount.core :as mount]
            [account-api-v2.middleware :as middleware]))

(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))

(def app-routes
  (routes
    #'service-routes
    (wrap-routes #'restricted-service-routes middleware/wrap-auth)
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))

(defn app [] (middleware/wrap-base #'app-routes))
