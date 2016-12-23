(ns account-api-v2.routes.services.auth
  (:require [account-api-v2.db.core :as db]
            [ring.util.http-response :as response]
            [buddy.hashers :as hashers]
            [clojure.tools.logging :as log]))

(defn handle-registration-error [e]
  (if (instance? com.mongodb.DuplicateKeyException e)
    (response/precondition-failed
     {:result :error
      :message "user with the selected email address already exists"})
    (do
      (log/error e)
      (response/internal-server-error
       {:result :error
        :message "server error ocurred while adding the user"}))))

(defn decode-auth [encoded]
  (let [auth (second (.split encoded " "))]
    (-> (.decode (java.util.Base64/getDecoder) auth)
        (String. (java.nio.charset.Charset/forName "UTF-8"))
        (.split ":"))))

(defn authenticate [[email pass]]
  (when-let [user (db/get-user email)]
    (when (hashers/check pass (:password user))
      (str (:_id user)))))

(defn login! [{:keys [session]} auth]
  (if-let [id (authenticate (decode-auth auth))]
    (-> {:result :ok}
        (response/ok)
        (assoc :session (assoc session :identity id)))
    (response/unauthorized {:result :unauthorized
                            :message "login failure"})))

(defn logout! []
  (-> {:result :ok}
      (response/ok)
      (assoc :session nil)))

(defn register! [{:keys [session]} user]
  (try
    (let [record  (db/create-user!
                   (-> user
                       (dissoc :password-confirm)
                       (update :password hashers/encrypt)))]
      (-> {:result :ok}
          (response/ok)
          (assoc :session (assoc session :identity (str (:_id record))))))
    (catch Exception e
      (handle-registration-error e))))
