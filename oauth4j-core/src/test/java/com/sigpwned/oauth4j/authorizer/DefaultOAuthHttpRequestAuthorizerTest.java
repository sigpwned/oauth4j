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
package com.sigpwned.oauth4j.authorizer;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Test;
import com.sigpwned.oauth4j.core.OAuthHttpRequestAuthorizer;
import com.sigpwned.oauth4j.core.authorizer.DefaultOAuthHttpRequestAuthorizer;
import com.sigpwned.oauth4j.core.model.OAuthFormParameter;
import com.sigpwned.oauth4j.core.model.OAuthHttpHeader;
import com.sigpwned.oauth4j.core.model.OAuthHttpRequest;
import com.sigpwned.oauth4j.core.model.OAuthQueryParameter;

public class DefaultOAuthHttpRequestAuthorizerTest {
  /**
   * @see <a href=
   *      "https://developer.twitter.com/en/docs/authentication/oauth-1-0a/creating-a-signature">https://developer.twitter.com/en/docs/authentication/oauth-1-0a/creating-a-signature</a>
   */
  @Test
  public void test() {
    final OAuthHttpRequestAuthorizer authorizer = new DefaultOAuthHttpRequestAuthorizer() {
      @Override
      protected long now() {
        return 1318622958L;
      }

      @Override
      protected String nonce() {
        return "kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg";
      }
    };

    final String consumerKey = "xvz1evFS4wEEPTGEFPHBog";
    final String consumerSecret = "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw";
    final String tokenSecret = "LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE";
    final String token = "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb";

    final OAuthHttpRequest request = OAuthHttpRequest.of("POST",
        "https://api.twitter.com/1.1/statuses/update.json",
        singletonList(OAuthQueryParameter.of("include_entities", "true")), emptyList(),
        singletonList(
            OAuthFormParameter.of("status", "Hello Ladies + Gentlemen, a signed OAuth request!")));

    OAuthHttpRequest signed =
        authorizer.authorize(request, consumerKey, consumerSecret, token, tokenSecret);

    String authorization =
        signed.getHeaders().stream().filter(h -> h.getName().equalsIgnoreCase("authorization"))
            .map(OAuthHttpHeader::getValue).findFirst().orElseThrow(AssertionError::new);

    assertThat(authorization, is(
        "OAuth oauth_consumer_key=\"xvz1evFS4wEEPTGEFPHBog\", oauth_nonce=\"kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg\", oauth_signature=\"hCtSmYh%2BiHYCEqBWrE7C7hYmtUk%3D\", oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"1318622958\", oauth_token=\"370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb\", oauth_version=\"1.0\""));
  }
}
