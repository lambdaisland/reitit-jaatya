(ns lambdaisland.reitit-jaatya.sitemap
  (:require [clojure.xml :refer [emit]]
            [clojure.string :as string])
  (:import java.util.Date))

(defn format-date [date]
  (let [fmt (java.text.SimpleDateFormat. "yyyy-MM-dd")]
    (.format fmt date)))

(defn generate [site-url paths & [{:keys [trailing-slash ignored-files sitemap-ignored-paths]
                                   :or {trailing-slash false}}]]
  (with-out-str
    (emit
      {:tag :urlset
       :attrs {:xmlns "http://www.sitemaps.org/schemas/sitemap/0.9"}
       :content
       (for [path paths]
         {:tag :url
          :content
          [{:tag :loc
            :content [(str site-url
                           path
                           (when (and trailing-slash (not (string/ends-with? path "/")))
                             "/"))]}
           #_{:tag :lastmod
            :content [(-> f (.lastModified) (Date.) format-date)]}]})})))
