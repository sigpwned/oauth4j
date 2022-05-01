/*-
 * =================================LICENSE_START==================================
 * oauth4j-server
 * ====================================SECTION=====================================
 * Copyright (C) 2022 Andy Boothe
 * ====================================SECTION=====================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ==================================LICENSE_END===================================
 */
package com.sigpwned.oauth4j.server.resource;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import com.sigpwned.oauth4j.core.OAuthHttpRequestAuthorizer;
import com.sigpwned.oauth4j.core.authorizer.DefaultOAuthHttpRequestAuthorizer;
import com.sigpwned.oauth4j.core.model.OAuthFormParameter;
import com.sigpwned.oauth4j.core.model.OAuthHttpRequest;
import com.sigpwned.oauth4j.core.model.OAuthQueryParameter;
import com.sigpwned.oauth4j.core.util.OAuth;
import com.sigpwned.oauth4j.server.AuthenticatedHandler;
import com.sigpwned.oauth4j.server.TokenStore;
import com.sigpwned.oauth4j.server.util.HttpRequests;

/**
 * A fully-baked implementation of the Twitter OAuth 1.0a flow.
 */
@Path(TwitterOAuth1Resource.BASE_PATH)
public class TwitterOAuth1Resource {
  /* default */ static final String DEFAULT_TWITTER_REQUEST_TOKEN_URL =
      "https://api.twitter.com/oauth/request_token";

  /* default */ static final String DEFAULT_TWITTER_AUTHENTICATE_URL =
      "https://api.twitter.com/oauth/authenticate";

  /* default */ static final String DEFAULT_TWITTER_ACCESS_TOKEN_URL =
      "https://api.twitter.com/oauth/access_token";

  /* default */ static final String BASE_PATH = "oauth/twitter/1";

  /* default */ static final String AUTHENTICATE = "authenticate";

  /* default */ static final String CALLBACK = "callback";

  private final String baseUrl;
  private final String consumerKey;
  private final String consumerSecret;
  private final TokenStore store;
  private final AuthenticatedHandler handler;
  private final OAuthHttpRequestAuthorizer authorizer;
  private final String twitterRequestTokenUrl;
  private final String twitterAuthenticateUrl;
  private final String twitterAccessTokenUrl;

  @Inject
  public TwitterOAuth1Resource(@Named("oauth4jBaseUrl") String baseUrl,
      @Named("oauth4jTwitterConsumerKey") String consumerKey,
      @Named("oauth4jTwitterConsumerSecret") String consumerSecret, TokenStore store,
      AuthenticatedHandler handler) {
    this(baseUrl, consumerKey, consumerSecret, store, handler,
        DefaultOAuthHttpRequestAuthorizer.INSTANCE, DEFAULT_TWITTER_REQUEST_TOKEN_URL,
        DEFAULT_TWITTER_AUTHENTICATE_URL, DEFAULT_TWITTER_ACCESS_TOKEN_URL);
  }

  /* default */ TwitterOAuth1Resource(String baseUrl, String consumerKey, String consumerSecret,
      TokenStore store, AuthenticatedHandler handler, OAuthHttpRequestAuthorizer authorizer,
      String twitterRequestTokenUrl, String twitterAuthenticateUrl, String twitterAccessTokenUrl) {
    this.baseUrl = baseUrl;
    this.consumerKey = consumerKey;
    this.consumerSecret = consumerSecret;
    this.store = store;
    this.handler = handler;
    this.authorizer = authorizer;
    this.twitterRequestTokenUrl = twitterRequestTokenUrl;
    this.twitterAuthenticateUrl = twitterAuthenticateUrl;
    this.twitterAccessTokenUrl = twitterAccessTokenUrl;
  }

  @Path(AUTHENTICATE)
  public Response authenticate() throws IOException {
    List<OAuthQueryParameter> queryParameters = new ArrayList<>();
    queryParameters.add(OAuthQueryParameter.of(OAuth.OAUTH_CALLBACK_NAME, getCallbackUrl()));

    OAuthHttpRequest unsignedRequest = OAuthHttpRequest.of(OAuthHttpRequest.POST_METHOD,
        getTwitterRequestTokenUrl(), queryParameters, emptyList(), emptyList());

    OAuthHttpRequest signedRequest =
        authorizer.authorize(unsignedRequest, getConsumerKey(), getConsumerSecret());

    HttpResponse<String> response;
    try {
      response = newHttpClient().send(HttpRequests.prepare(signedRequest),
          BodyHandlers.ofString(StandardCharsets.UTF_8));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new InterruptedIOException();
    }
    if (response.statusCode() != HttpURLConnection.HTTP_OK)
      throw new InternalServerErrorException();

    List<OAuthFormParameter> forms = Stream.of(response.body().split("&")).filter(s -> !s.isEmpty())
        .map(OAuthFormParameter::fromString).collect(toList());

    String oauthToken = forms.stream().filter(p -> p.getKey().equals(OAuth.OAUTH_TOKEN_NAME))
        .map(OAuthFormParameter::getValue).findFirst()
        .orElseThrow(InternalServerErrorException::new);

    String oauthTokenSecret =
        forms.stream().filter(p -> p.getKey().equals(OAuth.OAUTH_TOKEN_SECRET_NAME))
            .map(OAuthFormParameter::getValue).findFirst()
            .orElseThrow(InternalServerErrorException::new);

    getStore().putTokenSecret(oauthToken, oauthTokenSecret);

    return Response.temporaryRedirect(URI.create(getTwitterAuthenticateUrl())).build();
  }

  /**
   * Returns the local URL for the authenticate endpoint
   */
  public String getAuthenticateUrl() {
    return String.format("%s/%s/%s", getBaseUrl(), BASE_PATH, AUTHENTICATE);
  }

  @Path(CALLBACK)
  public Response callback(@QueryParam(OAuth.OAUTH_TOKEN_NAME) String oauthToken,
      @QueryParam(OAuth.OAUTH_VERIFIER_NAME) String oauthVerifier) throws IOException {
    String oauthTokenSecret =
        getStore().getTokenSecret(oauthToken).orElseThrow(NotFoundException::new);

    List<OAuthQueryParameter> queryParameters = new ArrayList<>();
    queryParameters.add(OAuthQueryParameter.of(OAuth.OAUTH_TOKEN_NAME, oauthToken));
    queryParameters.add(OAuthQueryParameter.of(OAuth.OAUTH_VERIFIER_NAME, oauthVerifier));

    OAuthHttpRequest unsignedRequest = OAuthHttpRequest.of(OAuthHttpRequest.POST_METHOD,
        getTwitterAccessTokenUrl(), queryParameters, emptyList(), emptyList());

    OAuthHttpRequest signedRequest = getAuthorizer().authorize(unsignedRequest, getConsumerKey(),
        getConsumerSecret(), oauthToken, oauthTokenSecret);

    HttpResponse<String> response;
    try {
      response = newHttpClient().send(HttpRequests.prepare(signedRequest),
          BodyHandlers.ofString(StandardCharsets.UTF_8));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new InterruptedIOException();
    }
    if (response.statusCode() != HttpURLConnection.HTTP_OK)
      throw new InternalServerErrorException();

    List<OAuthFormParameter> forms = Stream.of(response.body().split("&")).filter(s -> !s.isEmpty())
        .map(OAuthFormParameter::fromString).collect(toList());

    String accessToken = forms.stream().filter(p -> p.getKey().equals(OAuth.OAUTH_TOKEN_NAME))
        .map(OAuthFormParameter::getValue).findFirst()
        .orElseThrow(InternalServerErrorException::new);

    String accessTokenSecret =
        forms.stream().filter(p -> p.getKey().equals(OAuth.OAUTH_TOKEN_SECRET_NAME))
            .map(OAuthFormParameter::getValue).findFirst()
            .orElseThrow(InternalServerErrorException::new);

    return getHandler().authenticated(accessToken, accessTokenSecret);
  }

  /**
   * Returns the local URL for the callback endpoint
   */
  public String getCallbackUrl() {
    return String.format("%s/%s/%s", getBaseUrl(), BASE_PATH, CALLBACK);
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  /**
   * @return the store
   */
  public TokenStore getStore() {
    return store;
  }

  /**
   * @return the handler
   */
  public AuthenticatedHandler getHandler() {
    return handler;
  }

  /**
   * @return the consumerKey
   */
  public String getConsumerKey() {
    return consumerKey;
  }

  /**
   * @return the consumerSecret
   */
  public String getConsumerSecret() {
    return consumerSecret;
  }

  /**
   * @return the authorizer
   */
  public OAuthHttpRequestAuthorizer getAuthorizer() {
    return authorizer;
  }

  /**
   * @return the twitterRequestTokenUrl
   */
  public String getTwitterRequestTokenUrl() {
    return twitterRequestTokenUrl;
  }

  /**
   * @return the twitterAuthenticateUrl
   */
  public String getTwitterAuthenticateUrl() {
    return twitterAuthenticateUrl;
  }

  /**
   * @return the twitterAccessTokenUrl
   */
  public String getTwitterAccessTokenUrl() {
    return twitterAccessTokenUrl;
  }

  /**
   * test hook
   * 
   * @return
   */
  protected HttpClient newHttpClient() {
    return HttpClient.newHttpClient();
  }
}
