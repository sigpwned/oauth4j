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
package com.sigpwned.oauth4j.core.signer;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import com.sigpwned.oauth4j.core.OAuthHttpRequestSigner;
import com.sigpwned.oauth4j.core.model.OAuthHttpRequest;
import com.sigpwned.oauth4j.core.util.Encodings;
import com.sigpwned.oauth4j.core.util.OAuth;
import com.sigpwned.oauth4j.core.util.Parameter;

public class HmacSha1OAuthHttpRequestSigner implements OAuthHttpRequestSigner {
  public static final HmacSha1OAuthHttpRequestSigner INSTANCE =
      new HmacSha1OAuthHttpRequestSigner();

  @Override
  public String getOAuthSignatureMethod() {
    return OAuth.HMAC_SHA1_OAUTH_SIGNATURE_METHOD_VALUE;
  }

  protected byte[] computeSignatureBaseString(OAuthHttpRequest request, String oAuthNonce,
      long oAuthTimestamp, String oAuthVersion, String consumerKey, String token) {
    List<Parameter> parameters = new ArrayList<>();
    parameters.addAll(request.getQueryParameters().stream()
        .map(fp -> Parameter.of(fp.getKey(), fp.getValue())).collect(toList()));
    parameters.addAll(request.getFormParameters().stream()
        .map(fp -> Parameter.of(fp.getKey(), fp.getValue())).collect(toList()));
    parameters.add(Parameter.of(OAuth.OAUTH_CONSUMER_KEY_NAME, consumerKey));
    parameters.add(Parameter.of(OAuth.OAUTH_NONCE_NAME, oAuthNonce));
    parameters.add(Parameter.of(OAuth.OAUTH_SIGNATURE_METHOD_NAME, getOAuthSignatureMethod()));
    parameters.add(Parameter.of(OAuth.OAUTH_TIMESTAMP, Long.toString(oAuthTimestamp)));
    parameters.add(Parameter.of(OAuth.OAUTH_TOKEN_NAME, token));
    parameters.add(Parameter.of(OAuth.OAUTH_VERSION_NAME, oAuthVersion));

    String parameterString = parameters
        .stream().filter(p -> p.getValue() != null).sorted().map(p -> String.format("%s=%s",
            Encodings.urlencode(p.getKey()), Encodings.urlencode(p.getValue())))
        .collect(joining("&"));

    return new StringBuilder().append(request.getMethod().toUpperCase()).append("&")
        .append(Encodings.urlencode(request.getUrl())).append("&")
        .append(Encodings.urlencode(parameterString)).toString()
        .getBytes(StandardCharsets.US_ASCII);
  }

  protected byte[] computeSigningKey(String consumerSecret, String tokenSecret) {
    return new StringBuilder().append(Encodings.urlencode(consumerSecret)).append("&")
        .append(Encodings.urlencode(Optional.ofNullable(tokenSecret).orElse(""))).toString()
        .getBytes(StandardCharsets.US_ASCII);
  }

  private static final String ALGORITHM = "HmacSHA1";

  @Override
  public byte[] sign(OAuthHttpRequest request, String oAuthNonce, long oAuthTimestamp,
      String oAuthVersion, String consumerKey, String consumerSecret, String token,
      String tokenSecret) {
    byte[] signatureBase = computeSignatureBaseString(request, oAuthNonce, oAuthTimestamp,
        oAuthVersion, consumerKey, token);

    byte[] signingKey = computeSigningKey(consumerSecret, tokenSecret);

    Key key = new SecretKeySpec(signingKey, 0, signingKey.length, ALGORITHM);

    Mac mac;
    try {
      mac = Mac.getInstance(ALGORITHM);
    } catch (NoSuchAlgorithmException e) {
      // The spec stipulates JDKs must implement this algorithm
      throw new AssertionError("Required algorithm is not supported", e);
    }

    try {
      mac.init(key);
    } catch (InvalidKeyException e) {
      // They key and mac use the same value for the algorithm
      throw new AssertionError("Mac impossibly does not support Key with same algorithm", e);
    }

    return mac.doFinal(signatureBase);
  }
}
