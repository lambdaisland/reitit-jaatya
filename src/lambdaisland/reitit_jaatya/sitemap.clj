(ns lambdaisland.reitit-jaatya.sitemap
  (:require [clojure.xml :refer [emit]])
  (:import java.util.Date))

(defn format-date [date]
  (let [fmt (java.text.SimpleDateFormat. "yyyy-MM-dd")]
    (.format fmt date)))

(defn generate [site-url paths & [{:keys [ignored-files sitemap-ignored-paths]}]]
  (with-out-str
    (emit
      {:tag :urlset
       :attrs {:xmlns "http://www.sitemaps.org/schemas/sitemap/0.9"}
       :content
       (for [path paths]
         {:tag :url
          :content
          [{:tag :loc
            :content [(str site-url path)]}
           #_{:tag :lastmod
            :content [(-> f (.lastModified) (Date.) format-date)]}]})})))
