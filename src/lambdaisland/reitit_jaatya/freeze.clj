(ns lambdaisland.reitit-jaatya.freeze
  (:require [clojure.java.io :as io]
            [lambdaisland.reitit-jaatya.sitemap :as sitemap]
            [reitit.ring :as ring]
            [ring.mock.request :as mock]
            [reitit.core :as r]))

(defn get-router [handler]
  (-> handler meta ::r/router))

(defn freeze-page [path content & [{:keys [content-type]
                                    :or {content-type :html}}]]
  (println " > " path)
  (let [final-path (if (= content-type :html)
                     (str "_site" path "/index.html")
                     (str "_site" path))]
    (io/make-parents final-path)
    (spit final-path content)))

(defn create-sitemap [base-url path urls]
  (freeze-page path (sitemap/generate base-url urls) {:content-type :xml}))

(defn iced [handler {:keys [sitemap-path base-url]
                     :or {sitemap-path nil base-url ""}}]
  (let [router (get-router handler)
        routes (r/routes router)
        sitemap (atom [])]
    (doseq [[template {:keys [name freeze-data-fn freeze-content-type]
                       :or {freeze-content-type :html}}] routes]
      (let [freeze-data-fn (if (nil? freeze-data-fn)
                             (constantly [{}])
                             freeze-data-fn)]
        (println "Freezing pages @ " template)
        (doseq [path-params (freeze-data-fn)]
          (let [match (r/match-by-name router name path-params)
                path (:path match)
                resp (cond-> (mock/request :get path)
                       (= freeze-content-type :json)
                       (mock/header "accept" "application/json")
                       :always
                       handler)
                body (:body resp)
                content (cond
                          (string? body) body
                          :else
                          (slurp body))]
            (swap! sitemap conj path)
            (freeze-page path content {:content-type freeze-content-type})))))
    (when sitemap-path
      (create-sitemap base-url sitemap-path @sitemap))
    {:paths @sitemap}))

(comment
  (defn test-handler [data]
    {:status 200
     :body "test body"})

  (def router
    (ring/router
     ["/api"
      ["/ping" {:name ::ping :get test-handler :freeze-data-fn (fn []
                                                                 [{}])}]
      ["/user/:id/:name" {:name :user/id :get test-handler
                          :freeze-data-fn (fn []
                                            [{:id 1 :name "ox"}
                                             {:id 20 :name "cyborg"}])}]]))

  (def handler (ring/ring-handler router))

  (def mn
    (r/match-by-name (get-router handler) :user/id {:id 1 :name "ox"}))

  (r/routes (get-router handler))

  (iced handler)
  ,)
