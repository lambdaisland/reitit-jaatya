(ns lambdaisland.reitit-jaatya.freeze
  (:require [clojure.java.io :as io]
            [lambdaisland.reitit-jaatya.sitemap :as sitemap]
            [reitit.ring :as ring]
            [ring.mock.request :as mock]
            [reitit.core :as r]
            [clojure.string :as str]))

(defn get-router [handler]
  (-> handler meta ::r/router))

(defn freeze-page [path content & [{:keys [content-type build-dir]
                                    :or {content-type :html build-dir "_site"}}]]
  (println " > " path)
  (let [final-path (if (and (= content-type :html) (not (str/ends-with? path ".html")))
                     (str build-dir path "/index.html")
                     (str build-dir path))]
    (io/make-parents final-path)
    (spit final-path content)))

(defn iced [handler & [{:keys [sitemap-path sitemap-trailing-slash base-url build-dir]
                        :or {sitemap-path nil sitemap-trailing-slash false base-url "" build-dir "_site"}}]]
  (let [router (get-router handler)
        routes (r/routes router)
        sitemap (atom [])]
    (doseq [[template {:keys [name freeze-data-fn freeze-content-type no-freeze]
                       :or {freeze-content-type :html}}] routes
            :when (not no-freeze)]
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

                       (string? freeze-content-type)
                       (mock/header "accept" freeze-content-type)

                       :always
                       handler)
                body (:body resp)
                content (cond
                          (string? body) body
                          :else
                          (slurp body))]
            (swap! sitemap conj path)
            (freeze-page path content {:content-type freeze-content-type
                                       :build-dir build-dir})))))
    (when sitemap-path
      (freeze-page sitemap-path
                   (sitemap/generate base-url @sitemap {:trailing-slash sitemap-trailing-slash})
                   {:content-type :xml :build-dir build-dir}))
    {:paths @sitemap}))

(comment
  (defn test-handler [data]
    {:status 200
     :body "test body"})

  (def router
    (ring/router
     [
      ["/" {:name ::home :get test-handler}]
      ["/api"
       ["/ping" {:name ::ping :get test-handler :freeze-data-fn (fn []
                                                                  [{}])}]
       ["/user/:id/:name" {:name :user/id :get test-handler
                           :freeze-data-fn (fn []
                                             [{:id 1 :name "ox"}
                                              {:id 20 :name "cyborg"}])}]]]))

  (def handler (ring/ring-handler router))

  (def mn
    (r/match-by-name (get-router handler) :user/id {:id 1 :name "ox"}))

  (r/routes (get-router handler))

  ;; default build
  (iced handler)

  ;; customised build
  (iced handler {:sitemap-path "/sitemap"
                 :build-dir "_build"
                 :sitemap-trailing-slash true
                 :base-url "https://lambdaisland.com"})
  ,)
