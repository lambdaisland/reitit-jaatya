(ns reitit-jaatya.freeze
  (:require [clojure.java.io :as io]
            [reitit.ring :as ring]
            [ring.mock.request :as mock]
            [reitit.core :as r]))

(defn get-router [handler]
  (-> handler meta ::r/router))

(defn freeze-page [path content]
  (println " > " path)
  (let [final-path (str "_site" path "/index.html")]
    (io/make-parents final-path)
    (spit final-path content)))

(defn freeze-pages [pages template]
  (println "Freezing pages @ " template)
  (doseq [{:keys [path response]} pages]
    (freeze-page path (:body response))))

(defn iced [handler]
  (let [router (get-router handler)
        routes (r/routes router)]
    (doseq [[template {:keys [name freeze]}] routes]
      (let [freeze (if (nil? freeze)
                     (constantly [{}])
                     freeze)]
        (freeze-pages
         (into []
               (comp
                (map #(r/match-by-name router name %))
                (map #(->> (mock/request :get (:path %))
                           handler
                           (assoc {:path (:path %)} :response))))
               (freeze))
         template)))))

(comment
  (defn test-handler [data]
    {:status 200
     :body "test"})

  (def router
    (ring/router
     ["/api"
      ["/ping" {:name ::ping :get test-handler :freeze (fn []
                                                         [{}])}]
      ["/user/:id/:name" {:name :user/id :get test-handler
                          :freeze (fn []
                                    [{:id 1 :name "ox"}
                                     {:id 20 :name "cyborg"}])}]]))

  (def handler (ring/ring-handler router))

  (def mn
    (r/match-by-name (get-router handler) :user/id {:id 1 :name "ox"}))

  (iced handler)
  ,)
