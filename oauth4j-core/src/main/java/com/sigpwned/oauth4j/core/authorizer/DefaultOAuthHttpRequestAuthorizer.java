/*-
 * =================================LICENSE_START==================================
 * oauth4j-core
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
package com.sigpwned.oauth4j.core.authorizer;

import static java.util.stream.Collectors.joining;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import com.sigpwned.oauth4j.core.OAuthHttpRequestAuthorizer;
import com.sigpwned.oauth4j.core.OAuthHttpRequestSigner;
import com.sigpwned.oauth4j.core.model.OAuthHttpHeader;
import com.sigpwned.oauth4j.core.model.OAuthHttpRequest;
import com.sigpwned.oauth4j.core.signer.HmacSha1OAuthHttpRequestSigner;
import com.sigpwned.oauth4j.core.util.Encodings;
import com.sigpwned.oauth4j.core.util.OAuth;
import com.sigpwned.oauth4j.core.util.Parameter;

public class DefaultOAuthHttpRequestAuthorizer implements OAuthHttpRequestAuthorizer {
  public static final String DEFAULT_OAUTH_VERSION_VALUE = OAuth.ONE_DOT_OH_OAUTH_VERSION_VALUE;

  public static final OAuthHttpRequestSigner DEFAULT_SIGNER =
      HmacSha1OAuthHttpRequestSigner.INSTANCE;

  public static final DefaultOAuthHttpRequestAuthorizer INSTANCE =
      new DefaultOAuthHttpRequestAuthorizer();

  private final OAuthHttpRequestSigner signer;
  private final String oAuthVersion;

  public DefaultOAuthHttpRequestAuthorizer() {
    this(DEFAULT_SIGNER, DEFAULT_OAUTH_VERSION_VALUE);
  }

  public DefaultOAuthHttpRequestAuthorizer(OAuthHttpRequestSigner signer, String oAuthVersion) {
    this.signer = signer;
    this.oAuthVersion = oAuthVersion;
  }

  /**
   * @return the signer
   */
  public OAuthHttpRequestSigner getSigner() {
    return signer;
  }

  /**
   * @return the oAuthVersion
   */
  public String getOAuthVersion() {
    return oAuthVersion;
  }

  @Override
  public OAuthHttpRequest authorize(OAuthHttpRequest request, String consumerKey,
      String consumerSecret, String token, String tokenSecret) {

    long timestamp = now();

    String nonce = nonce();

    byte[] signature = getSigner().sign(request, nonce, timestamp, getOAuthVersion(), consumerKey,
        consumerSecret, token, tokenSecret);
    String signatureString = Base64.getEncoder().encodeToString(signature);

    List<Parameter> parameters = new ArrayList<>();
    parameters.add(Parameter.of(OAuth.OAUTH_CONSUMER_KEY_NAME, consumerKey));
    parameters.add(Parameter.of(OAuth.OAUTH_NONCE_NAME, nonce));
    parameters.add(Parameter.of(OAuth.OAUTH_SIGNATURE_NAME, signatureString));
    parameters.add(
        Parameter.of(OAuth.OAUTH_SIGNATURE_METHOD_NAME, getSigner().getOAuthSignatureMethod()));
    parameters.add(Parameter.of(OAuth.OAUTH_TIMESTAMP, Long.toString(timestamp)));
    parameters.add(Parameter.of(OAuth.OAUTH_VERSION_NAME, getOAuthVersion()));
    if (token != null)
      parameters.add(Parameter.of(OAuth.OAUTH_TOKEN_NAME, token));

    String authorization =
        "OAuth "
            + parameters
                .stream().sorted().map(p -> String.format("%s=\"%s\"",
                    Encodings.urlencode(p.getKey()), Encodings.urlencode(p.getValue())))
                .collect(joining(", "));

    List<OAuthHttpHeader> headers = new ArrayList<>();
    headers.addAll(request.getHeaders());
    headers.add(OAuthHttpHeader.of("Authorization", authorization));

    return OAuthHttpRequest.of(request.getMethod(), request.getUrl(), request.getQueryParameters(),
        headers, request.getFormParameters());
  }

  /**
   * test hook
   */
  protected long now() {
    return Instant.now().toEpochMilli();
  }

  private static final SecureRandom RANDOM = new SecureRandom();

  /**
   * test hook
   * 
   * @return
   */
  protected String nonce() {
    byte[] nonce = new byte[16];
    RANDOM.nextBytes(nonce);
    return Base64.getEncoder().encodeToString(nonce);
  }
}
