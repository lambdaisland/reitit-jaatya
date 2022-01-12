# reitit-jäätyä

<!-- badges -->
[![cljdoc badge](https://cljdoc.org/badge/com.lambdaisland/reitit-jaatya)](https://cljdoc.org/d/com.lambdaisland/reitit-jaatya) [![Clojars Project](https://img.shields.io/clojars/v/com.lambdaisland/reitit-jaatya.svg)](https://clojars.org/com.lambdaisland/reitit-jaatya)
<!-- /badges -->

> reitit means *routes* and *jäätyä* means freeze

Freeze your reitit routes and create a static site out of it.

## Why?

This library will allow you to create a static website out of your *existing*
dynamic website as long as you're using reitit ;)

## How?

Point jaatya to your reitit routes map and let it start the freezing process.

Jaatya will fake an http request and pass it along to the handler to record it's
response. If a response is `200` then a new html page is created based on the
url route.

## Features

<!-- installation -->
## Installation

To use the latest release, add the following to your `deps.edn` ([Clojure CLI](https://clojure.org/guides/deps_and_cli))

```
com.lambdaisland/reitit-jaatya {:mvn/version "0.0.23"}
```

or add the following to your `project.clj` ([Leiningen](https://leiningen.org/))

```
[com.lambdaisland/reitit-jaatya "0.0.23"]
```
<!-- /installation -->

## Rationale

`reitit-jaatya` uses the reitit routes as the canonical source of the final website structure.

Given a HTTP handler and reitit routes definition, jaatya will mock HTTP requests to each route
and create static files out of the successful HTTP responses.

There could be several reason why doing this might be better than simply using a traditional SSG:

1. It allows the flexibility of building the website as a traditional server garnering all the benefits like tooling, repl driven development, no need to worry about startup times, etc.
2. It gives the option to have a backend server always running (be it for admin dashboards, CMS, scheduling services, etc)
3. Not sure if your website might need dynamic behaviour in the future? No need to decide now and lock yourself into a static site generator. Just build it as a dynamic site from the get-go but deploy the frozen version until you are ready to commit.
4. Selectievely convert parts of your site to static version for higher performance

## Usage

This sample code shows how to use reitit-jaatya in a project.

``` clojure
(ns mywebsite
  (:require [reitit.ring :as ring]
            [reitit.core :as r]))

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

;; default build
;; creates the site in `_site` directory with no sitemap and no base-url in sitemap
(iced handler)

;; customised build
(iced handler {:sitemap-path "/sitemap"
                :build-dir "_build"
                :base-url "https://lambdaisland.com"})
```


<!-- opencollective -->
## Lambda Island Open Source

<img align="left" src="https://github.com/lambdaisland/open-source/raw/master/artwork/lighthouse_readme.png">

&nbsp;

reitit-jaatya is part of a growing collection of quality Clojure libraries created and maintained
by the fine folks at [Gaiwan](https://gaiwan.co).

Pay it forward by [becoming a backer on our Open Collective](http://opencollective.com/lambda-island),
so that we may continue to enjoy a thriving Clojure ecosystem.

You can find an overview of our projects at [lambdaisland/open-source](https://github.com/lambdaisland/open-source).

&nbsp;

&nbsp;
<!-- /opencollective -->

<!-- contributing -->
## Contributing

Everyone has a right to submit patches to reitit-jaatya, and thus become a contributor.

Contributors MUST

- adhere to the [LambdaIsland Clojure Style Guide](https://nextjournal.com/lambdaisland/clojure-style-guide)
- write patches that solve a problem. Start by stating the problem, then supply a minimal solution. `*`
- agree to license their contributions as MPL 2.0.
- not break the contract with downstream consumers. `**`
- not break the tests.

Contributors SHOULD

- update the CHANGELOG and README.
- add tests for new functionality.

If you submit a pull request that adheres to these rules, then it will almost
certainly be merged immediately. However some things may require more
consideration. If you add new dependencies, or significantly increase the API
surface, then we need to decide if these changes are in line with the project's
goals. In this case you can start by [writing a pitch](https://nextjournal.com/lambdaisland/pitch-template),
and collecting feedback on it.

`*` This goes for features too, a feature needs to solve a problem. State the problem it solves, then supply a minimal solution.

`**` As long as this project has not seen a public release (i.e. is not on Clojars)
we may still consider making breaking changes, if there is consensus that the
changes are justified.
<!-- /contributing -->

<!-- license -->
## License

Copyright &copy; 2021 Arne Brasseur and Contributors

Licensed under the term of the Mozilla Public License 2.0, see LICENSE.
<!-- /license -->
