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

@Path(TwitterOAuth1Resource.BASE_PATH)
public class TwitterOAuth1Resource {
  public static final String BASE_PATH = "oauth/twitter/1";
  public static final String AUTHENTICATE = "authenticate";
  public static final String CALLBACK = "callback";

  private final String baseUrl;
  private final String consumerKey;
  private final String consumerSecret;
  private final TokenStore store;
  private final AuthenticatedHandler handler;
  private final OAuthHttpRequestAuthorizer authorizer;

  @Inject
  public TwitterOAuth1Resource(String baseUrl, String consumerKey, String consumerSecret,
      TokenStore store, AuthenticatedHandler handler) {
    this(baseUrl, consumerKey, consumerSecret, store, handler,
        DefaultOAuthHttpRequestAuthorizer.INSTANCE);
  }

  /* default */ TwitterOAuth1Resource(String baseUrl, String consumerKey, String consumerSecret,
      TokenStore store, AuthenticatedHandler handler, OAuthHttpRequestAuthorizer authorizer) {
    this.baseUrl = baseUrl;
    this.consumerKey = consumerKey;
    this.consumerSecret = consumerSecret;
    this.store = store;
    this.handler = handler;
    this.authorizer = authorizer;
  }

  @Path(AUTHENTICATE)
  public Response authenticate() throws IOException {
    HttpClient client = HttpClient.newHttpClient();

    String oauthCallback = String.format("%s/%s/%s", getBaseUrl(), BASE_PATH, CALLBACK);

    List<OAuthQueryParameter> queryParameters = new ArrayList<>();
    queryParameters.add(OAuthQueryParameter.of(OAuth.OAUTH_CALLBACK_NAME, oauthCallback));

    OAuthHttpRequest unsignedRequest = OAuthHttpRequest.of(OAuthHttpRequest.POST_METHOD,
        "https://api.twitter.com/oauth/request_token", queryParameters, emptyList(), emptyList());

    OAuthHttpRequest signedRequest =
        authorizer.authorize(unsignedRequest, consumerKey, consumerSecret);

    HttpResponse<String> response;
    try {
      response = client.send(HttpRequests.prepare(signedRequest),
          BodyHandlers.ofString(StandardCharsets.UTF_8));
    } catch (InterruptedException e) {
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

    return Response.temporaryRedirect(URI.create("https://api.twitter.com/oauth/authenticate"))
        .build();
  }

  @Path(CALLBACK)
  public Response callback(@QueryParam(OAuth.OAUTH_TOKEN_NAME) String oauthToken,
      @QueryParam(OAuth.OAUTH_VERIFIER_NAME) String oauthVerifier) throws IOException {
    String oauthTokenSecret =
        getStore().getTokenSecret(oauthToken).orElseThrow(NotFoundException::new);

    HttpClient client = HttpClient.newHttpClient();

    List<OAuthQueryParameter> queryParameters = new ArrayList<>();
    queryParameters.add(OAuthQueryParameter.of(OAuth.OAUTH_TOKEN_NAME, oauthToken));
    queryParameters.add(OAuthQueryParameter.of(OAuth.OAUTH_VERIFIER_NAME, oauthToken));

    OAuthHttpRequest unsignedRequest = OAuthHttpRequest.of(OAuthHttpRequest.POST_METHOD,
        "https://api.twitter.com/oauth/access_token", queryParameters, emptyList(), emptyList());

    OAuthHttpRequest signedRequest = authorizer.authorize(unsignedRequest, consumerKey,
        consumerSecret, oauthToken, oauthTokenSecret);

    HttpResponse<String> response;
    try {
      response = client.send(HttpRequests.prepare(signedRequest),
          BodyHandlers.ofString(StandardCharsets.UTF_8));
    } catch (InterruptedException e) {
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
}
