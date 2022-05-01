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
