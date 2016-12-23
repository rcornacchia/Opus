(ns account-api-v2.db.core
    (:require [monger.core :as mg]
              [monger.collection :as mc]
              [monger.operators :refer :all]
              [mount.core :refer [defstate]]
              [account-api-v2.config :refer [env]])
    (:import org.bson.types.ObjectId))

(defstate db*
  :start (-> env :database-url mg/connect-via-uri)
  :stop (-> db* :conn mg/disconnect))

(defstate db
  :start (:db db*))

(defn create-user! [user]
  (let [oid (ObjectId.)
        m (merge user {:_id oid})]
    (mc/insert-and-return db "users" m)))

(defn update-user! [id first-name last-name email]
  (mc/update db "users" {:_id (ObjectId. id)}
             {$set {:first_name first-name
                    :last_name last-name
                    :email email}}))

(defn save-profile-picture! [{:keys [id picture-type picture-name picture-data] :as picture-map}]
  (mc/update db "users" {:_id (ObjectId. id)}
             {$set {:profile_picture (dissoc picture-map :id)}}))

(defn get-user [email]
  (mc/find-one-as-map db "users" {:email email}))

(defn remove-user! [id]
  (let [oid (ObjectId. id)]
    (mc/remove-by-id db "users" oid)))
