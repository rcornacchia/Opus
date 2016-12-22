(ns account-api-v2.routes.services
  (:require [account-api-v2.routes.services.auth :as auth]
            [account-api-v2.routes.services.upload :as upload]
            [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [compojure.api.upload :refer [wrap-multipart-params TempFileUpload]]
            [schema.core :as s]
            [compojure.api.meta :refer [restructure-param]]
            [buddy.auth.accessrules :refer [restrict]]
            [buddy.auth :refer [authenticated?]]))

(defn access-error [_ _]
  (unauthorized {:error "unauthorized"}))

(defn wrap-restricted [handler rule]
  (restrict handler {:handler  rule
                     :on-error access-error}))

(defmethod restructure-param :auth-rules
  [_ rule acc]
  (update-in acc [:middleware] conj [wrap-restricted rule]))

(defmethod restructure-param :current-user
  [_ binding acc]
  (update-in acc [:letks] into [binding `(:identity ~'+compojure-api-request+)]))

(s/defschema UserRegistration
  {:password String
   :password-confirm String
   :name String
   :street_address String
   :phone_number String
   :email String
   :artists String
   :bio String})

(s/defschema Result
  {:result s/Keyword
   (s/optional-key :message) String})

(defapi restricted-service-routes
  {:swagger {:ui "/swagger-ui-private"
             :spec "/swagger-private.json"
             :data {:info {:version "1.0.0"
                           :title "Profile Picture API"
                           :description "Private Services"}}}}
  (POST "/upload" req
        :multipart-params [file :- TempFileUpload]
        :middleware [wrap-multipart-params]
        :summary "handles image upload"
        :return Result
        (println req)
        (upload/save-image! (:identity req) file)))

(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version "1.0.0"
                           :title "Sample API"
                           :description "Sample Services"}}}}

  (GET "/authenticated" []
       :auth-rules authenticated?
       :current-user user
       (ok {:user (dissoc user :_id)}))
  (context "/api" []
    :tags ["account registration"]

    (POST "/register" req
          :return Result
          :body [user UserRegistration]
          :summary "register new user"
          (auth/register! req user))
    (POST "/login" req
          :header-params [authorization :- String]
          :summary "log in the user and create session"
          :return Result
          (auth/login! req authorization))
    (POST "/logout" []
          :summary "remove user session"
          :return Result
          (auth/logout!))))
