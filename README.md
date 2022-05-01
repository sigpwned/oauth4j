# OAUTH4J [![tests](https://github.com/sigpwned/oauth4j/actions/workflows/tests.yml/badge.svg)](https://github.com/sigpwned/oauth4j/actions/workflows/tests.yml) [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=sigpwned_oauth4j&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=sigpwned_oauth4j) [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=sigpwned_oauth4j&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=sigpwned_oauth4j) [![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=sigpwned_oauth4j&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=sigpwned_oauth4j)

OAuth4j is a framework for implementing OAuth flows with
implementations for major social networks using JAX-RS.

## Goals

* Provide useful tools for creating OAuth implementations
* Provide implementations of the most common OAuth flows using JAX-RS

## Non-Goals

* Provide implementations of all OAuth flows

## Code Examples

### Creating New Implementations

Web applications wanting to implement new OAuth flows should add the
Maven dependency:

    <dependency>
      <groupId>com.sigpwned</groupId>
      <artifactId>oauth4j-core</artifactId>
      <version>0.0.0</version>
    </dependency>

This project includes a model of an HTTP request `OAuthHttpRequest`
plus code to generate a signature and sign the request. These are the
core mechanics of implementing a new OAuth flow. A good example of
putting this code to work is in `TwitterOAuth1Resource#authenticate`.

Note that the `OAuthHttpRequest` is not an actual client object used
for sending HTTP requests, but rather a neutral model used for
generating signatures. The user must provide code to convert client
HTTP requests back and forth from this model to use the code. A good
example of a conversion routine is in `HttpRequests#prepare`.

### Using Provided Implementations

Web applications wanting to add OAuth implementations should add the
dependency:

    <dependency>
      <groupId>com.sigpwned</groupId>
      <artifactId>oauth4j-server</artifactId>
      <version>0.0.0</version>
    </dependency>

#### Twitter Implementation

To use the Twitter implementation, web applications should register
the `TwitterOAuth1Resource` class with the server engine as a new
resource class. This adds the following new endpoints to the web
application:

* `${baseUrl}/oauth/twitter/1/authenticate`
* `${baseUrl}/oauth/twitter/1/callback`

Users must also register the following values for dependency
injection:

* `@Named("oauth4jBaseUrl") String` -- The base URL of the whole web
  application, e.g., `https://api.example.com/v1`.
* `@Named("oauth4jTwitterConsumerKey") String` -- The consumer key of
  the Twitter application
* `@Named("oauth4jTwitterConsumerSecret") String` -- The consumer
  secret of the Twitter application
* `TokenStore` -- A store for OAuth tokens generated during the flow
* `AuthenticatedHandler` -- A handler for generated access tokens

The web application should send users to the
`${baseUrl}/oauth/twitter/1/authenticate` endpoint to authenticate
them.