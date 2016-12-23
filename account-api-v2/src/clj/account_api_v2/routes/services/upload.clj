(ns account-api-v2.routes.services.upload
  (:require [account-api-v2.db.core :as db]
            [ring.util.http-response :refer :all]
            [clojure.tools.logging :as log])
  (:import [java.awt.image AffineTransformOp BufferedImage]
           [java.io ByteArrayOutputStream FileInputStream]
           java.awt.geom.AffineTransform
           javax.imageio.ImageIO
           java.net.URLEncoder))

(def thumb-size 150)

(def thumb-prefix "thumb_")

(defn file->bytearray [x]
  (with-open [input (FileInputStream. x)
              buffer (ByteArrayOutputStream.)]
    (clojure.java.io/copy input buffer)
    (.toByteArray buffer)))

(defn scale [img ratio width height]
  (let [scale (AffineTransform/getScaleInstance
               (double ratio) (double ratio))
        transform-op (AffineTransformOp. scale AffineTransformOp/TYPE_BILINEAR)]
    (.filter transform-op img (BufferedImage. width height (.getType img)))))

(defn scale-image [file thumb-size]
  (let [img (ImageIO/read file)
        img-width (.getWidth img)
        img-height (.getHeight img)
        ratio (/ thumb-size img-height)]
    (scale img ratio (int (* img-width ratio)) thumb-size)))

(defn image->byte-array [image]
  (let [baos (ByteArrayOutputStream.)]
    (ImageIO/write image "png" baos)
    (.toByteArray baos)))

(defn save-image! [id {:keys [tempfile filename content-type]}]
  (try
    (let [db-file-name (str id (.replaceAll filename "[^a-zA-Z0-9-_\\.]" ""))]
      (db/save-profile-picture! {:id id
                                 :picture-type content-type
                                 :picture-name db-file-name
                                 :picture-data (file->bytearray tempfile)})
      (db/save-profile-picture! {:id id
                                 :picture-type "image/png"
                                 :picture-data (image->byte-array
                                                (scale-image tempfile thumb-size))
                                 :picture-name (str thumb-prefix db-file-name)}))
    (ok {:result :ok})
    (catch Exception e
      (log/error e)
      (internal-server-error "error"))))
