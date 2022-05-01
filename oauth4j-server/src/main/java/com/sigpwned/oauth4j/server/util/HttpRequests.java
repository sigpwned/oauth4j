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
package com.sigpwned.oauth4j.server.util;

import static java.util.stream.Collectors.joining;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import com.sigpwned.oauth4j.core.model.OAuthHttpHeader;
import com.sigpwned.oauth4j.core.model.OAuthHttpRequest;

public final class HttpRequests {
  private HttpRequests() {}

  public static HttpRequest prepare(OAuthHttpRequest request) {
    StringBuilder uri = new StringBuilder().append(request.getUrl());
    if (!request.getQueryParameters().isEmpty()) {
      uri.append("?").append(
          request.getQueryParameters().stream().map(Objects::toString).collect(joining("&")));
    }

    HttpRequest.Builder result = HttpRequest.newBuilder().uri(URI.create(uri.toString()));

    for (OAuthHttpHeader header : request.getHeaders())
      result.header(header.getName(), header.getValue());

    switch (request.getMethod()) {
      case OAuthHttpRequest.POST_METHOD:
        result =
            result
                .POST(BodyPublishers.ofString(request.getFormParameters().stream()
                    .map(Objects::toString).collect(joining("&")), StandardCharsets.UTF_8))
                .header("Content-Type", "application/x-ww-form-urlencoded; charset=utf-8");
        break;
      default:
        throw new AssertionError("unrecognized method " + request.getMethod());
    }

    return result.build();
  }
}
