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
package com.sigpwned.oauth4j.core;

import com.sigpwned.oauth4j.core.model.OAuthHttpRequest;

public interface OAuthHttpRequestAuthorizer {
  public static final String NO_TOKEN = null;

  public static final String NO_TOKEN_SECRET = null;

  public OAuthHttpRequest authorize(OAuthHttpRequest request, String consumerKey,
      String consumerSecret, String token, String tokenSecret);

  default OAuthHttpRequest authorize(OAuthHttpRequest request, String consumerKey,
      String consumerSecret) {
    return authorize(request, consumerKey, consumerSecret, NO_TOKEN, NO_TOKEN_SECRET);
  }
}
