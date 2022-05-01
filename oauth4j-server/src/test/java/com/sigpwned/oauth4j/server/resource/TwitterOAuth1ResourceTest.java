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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Optional;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.sigpwned.oauth4j.core.authorizer.DefaultOAuthHttpRequestAuthorizer;
import com.sigpwned.oauth4j.core.util.OAuth;
import com.sigpwned.oauth4j.core.util.Parameter;
import com.sigpwned.oauth4j.server.AuthenticatedHandler;
import com.sigpwned.oauth4j.server.TokenStore;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class TwitterOAuth1ResourceTest {
  public MockWebServer server;

  @Before
  public void setupDefaultLinkUnwinderTest() {
    server = new MockWebServer();
  }

  @After
  public void cleanupDefaultLinkUnwinderTest() throws IOException {
    server.shutdown();
  }

  /**
   * A good flow should succeed
   */
  @Test
  public void successTest() throws Exception {
    final String oauthToken = "foo";
    final String oauthTokenSecret = "bar";
    final String oauthTokenVerifier = "verifier";
    final String consumerKey = "xvz1evFS4wEEPTGEFPHBog";
    final String consumerSecret = "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw";
    final String token = "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb";
    final String tokenSecret = "LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE";

    server.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
        .setBody(String.format("%s&%s", Parameter.of(OAuth.OAUTH_TOKEN_NAME, oauthToken),
            Parameter.of(OAuth.OAUTH_TOKEN_SECRET_NAME, oauthTokenSecret))));

    server.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
        .setBody(String.format("%s&%s", Parameter.of(OAuth.OAUTH_TOKEN_NAME, token),
            Parameter.of(OAuth.OAUTH_TOKEN_SECRET_NAME, tokenSecret))));

    server.start();

    final TokenStore store = mock(TokenStore.class);
    when(store.getTokenSecret(oauthToken)).thenReturn(Optional.of(oauthTokenSecret));

    final int status = HttpURLConnection.HTTP_OK;
    final AuthenticatedHandler handler = mock(AuthenticatedHandler.class);
    when(handler.authenticated(token, tokenSecret)).thenReturn(Response.status(status).build());

    final HttpUrl twitterRequestTokenUrl = server.url(TwitterOAuth1Resource.BASE_PATH + "/"
        + TwitterOAuth1Resource.DEFAULT_TWITTER_REQUEST_TOKEN_URL);
    final HttpUrl twitterAuthenticateUrl = server.url(TwitterOAuth1Resource.BASE_PATH + "/"
        + TwitterOAuth1Resource.DEFAULT_TWITTER_AUTHENTICATE_URL);
    final HttpUrl twitterAccessTokenUrl = server.url(TwitterOAuth1Resource.BASE_PATH + "/"
        + TwitterOAuth1Resource.DEFAULT_TWITTER_ACCESS_TOKEN_URL);

    TwitterOAuth1Resource unit =
        new TwitterOAuth1Resource("http://localhost:8080", consumerKey, consumerSecret, store,
            handler, DefaultOAuthHttpRequestAuthorizer.INSTANCE, twitterRequestTokenUrl.toString(),
            twitterAuthenticateUrl.toString(), twitterAccessTokenUrl.toString());

    Response response1 = unit.authenticate();

    verify(store).putTokenSecret(oauthToken, oauthTokenSecret);

    assertThat(response1.getStatus(), is(307));
    assertThat(response1.getHeaderString("location"), is(twitterAuthenticateUrl.toString()));

    RecordedRequest request1 = server.takeRequest();
    assertThat(request1.getRequestUrl().queryParameter(OAuth.OAUTH_CALLBACK_NAME),
        is(unit.getCallbackUrl()));

    Response response2 = unit.callback(oauthToken, oauthTokenVerifier);

    assertThat(response2.getStatus(), is(status));
  }

  /**
   * We should 404 if we get an unrecognized oauth token
   * 
   * @throws InterruptedException
   */
  @Test(expected = NotFoundException.class)
  public void unrecognizedOauthTokenTest() throws Exception {
    final String oauthToken = "foo";
    final String oauthTokenSecret = "bar";
    final String oauthTokenVerifier = "verifier";
    final String consumerKey = "xvz1evFS4wEEPTGEFPHBog";
    final String consumerSecret = "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw";
    final String token = "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb";
    final String tokenSecret = "LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE";

    server.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
        .setBody(String.format("%s&%s", Parameter.of(OAuth.OAUTH_TOKEN_NAME, oauthToken),
            Parameter.of(OAuth.OAUTH_TOKEN_SECRET_NAME, oauthTokenSecret))));

    server.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
        .setBody(String.format("%s&%s", Parameter.of(OAuth.OAUTH_TOKEN_NAME, token),
            Parameter.of(OAuth.OAUTH_TOKEN_SECRET_NAME, tokenSecret))));

    server.start();

    final TokenStore store = mock(TokenStore.class);
    when(store.getTokenSecret(oauthToken)).thenReturn(Optional.of(oauthTokenSecret));

    final AuthenticatedHandler handler = mock(AuthenticatedHandler.class);
    when(handler.authenticated(token, tokenSecret)).thenReturn(Response.ok().build());

    final HttpUrl twitterRequestTokenUrl = server.url(TwitterOAuth1Resource.BASE_PATH + "/"
        + TwitterOAuth1Resource.DEFAULT_TWITTER_REQUEST_TOKEN_URL);
    final HttpUrl twitterAuthenticateUrl = server.url(TwitterOAuth1Resource.BASE_PATH + "/"
        + TwitterOAuth1Resource.DEFAULT_TWITTER_AUTHENTICATE_URL);
    final HttpUrl twitterAccessTokenUrl = server.url(TwitterOAuth1Resource.BASE_PATH + "/"
        + TwitterOAuth1Resource.DEFAULT_TWITTER_ACCESS_TOKEN_URL);

    TwitterOAuth1Resource unit =
        new TwitterOAuth1Resource("http://localhost:8080", consumerKey, consumerSecret, store,
            handler, DefaultOAuthHttpRequestAuthorizer.INSTANCE, twitterRequestTokenUrl.toString(),
            twitterAuthenticateUrl.toString(), twitterAccessTokenUrl.toString());

    Response response1 = unit.authenticate();

    verify(store).putTokenSecret(oauthToken, oauthTokenSecret);

    assertThat(response1.getStatus(), is(307));
    assertThat(response1.getHeaderString("location"), is(twitterAuthenticateUrl.toString()));

    RecordedRequest request1 = server.takeRequest();
    assertThat(request1.getRequestUrl().queryParameter(OAuth.OAUTH_CALLBACK_NAME),
        is(unit.getCallbackUrl()));

    unit.callback("barf", oauthTokenVerifier);
  }

  /**
   * We should 500 if we get a weird response from Twitter API
   */
  @Test(expected = InternalServerErrorException.class)
  public void failedRequestTokenTest() throws Exception {
    final String oauthToken = "foo";
    final String oauthTokenSecret = "bar";
    final String consumerKey = "xvz1evFS4wEEPTGEFPHBog";
    final String consumerSecret = "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw";
    final String token = "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb";
    final String tokenSecret = "LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE";

    server.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR));

    server.start();

    final TokenStore store = mock(TokenStore.class);
    when(store.getTokenSecret(oauthToken)).thenReturn(Optional.of(oauthTokenSecret));

    final AuthenticatedHandler handler = mock(AuthenticatedHandler.class);
    when(handler.authenticated(token, tokenSecret)).thenReturn(Response.ok().build());

    final HttpUrl twitterRequestTokenUrl = server.url(TwitterOAuth1Resource.BASE_PATH + "/"
        + TwitterOAuth1Resource.DEFAULT_TWITTER_REQUEST_TOKEN_URL);
    final HttpUrl twitterAuthenticateUrl = server.url(TwitterOAuth1Resource.BASE_PATH + "/"
        + TwitterOAuth1Resource.DEFAULT_TWITTER_AUTHENTICATE_URL);
    final HttpUrl twitterAccessTokenUrl = server.url(TwitterOAuth1Resource.BASE_PATH + "/"
        + TwitterOAuth1Resource.DEFAULT_TWITTER_ACCESS_TOKEN_URL);

    TwitterOAuth1Resource unit =
        new TwitterOAuth1Resource("http://localhost:8080", consumerKey, consumerSecret, store,
            handler, DefaultOAuthHttpRequestAuthorizer.INSTANCE, twitterRequestTokenUrl.toString(),
            twitterAuthenticateUrl.toString(), twitterAccessTokenUrl.toString());

    unit.authenticate();
  }

  /**
   * We should 500 if we get a weird response from Twitter API
   */
  @Test(expected = InternalServerErrorException.class)
  public void failedAccessTokenTest() throws Exception {
    final String oauthToken = "foo";
    final String oauthTokenSecret = "bar";
    final String oauthTokenVerifier = "verifier";
    final String consumerKey = "xvz1evFS4wEEPTGEFPHBog";
    final String consumerSecret = "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw";
    final String token = "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb";
    final String tokenSecret = "LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE";

    server.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
        .setBody(String.format("%s&%s", Parameter.of(OAuth.OAUTH_TOKEN_NAME, oauthToken),
            Parameter.of(OAuth.OAUTH_TOKEN_SECRET_NAME, oauthTokenSecret))));

    server.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR));

    server.start();

    final TokenStore store = mock(TokenStore.class);
    when(store.getTokenSecret(oauthToken)).thenReturn(Optional.of(oauthTokenSecret));

    final AuthenticatedHandler handler = mock(AuthenticatedHandler.class);
    when(handler.authenticated(token, tokenSecret)).thenReturn(Response.ok().build());

    final HttpUrl twitterRequestTokenUrl = server.url(TwitterOAuth1Resource.BASE_PATH + "/"
        + TwitterOAuth1Resource.DEFAULT_TWITTER_REQUEST_TOKEN_URL);
    final HttpUrl twitterAuthenticateUrl = server.url(TwitterOAuth1Resource.BASE_PATH + "/"
        + TwitterOAuth1Resource.DEFAULT_TWITTER_AUTHENTICATE_URL);
    final HttpUrl twitterAccessTokenUrl = server.url(TwitterOAuth1Resource.BASE_PATH + "/"
        + TwitterOAuth1Resource.DEFAULT_TWITTER_ACCESS_TOKEN_URL);

    TwitterOAuth1Resource unit =
        new TwitterOAuth1Resource("http://localhost:8080", consumerKey, consumerSecret, store,
            handler, DefaultOAuthHttpRequestAuthorizer.INSTANCE, twitterRequestTokenUrl.toString(),
            twitterAuthenticateUrl.toString(), twitterAccessTokenUrl.toString());

    Response response1 = unit.authenticate();

    verify(store).putTokenSecret(oauthToken, oauthTokenSecret);

    assertThat(response1.getStatus(), is(307));
    assertThat(response1.getHeaderString("location"), is(twitterAuthenticateUrl.toString()));

    RecordedRequest request1 = server.takeRequest();
    assertThat(request1.getRequestUrl().queryParameter(OAuth.OAUTH_CALLBACK_NAME),
        is(unit.getCallbackUrl()));

    unit.callback(oauthToken, oauthTokenVerifier);
  }
}
