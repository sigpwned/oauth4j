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
package com.sigpwned.oauth4j.core.model;

import static java.util.Collections.unmodifiableList;
import java.util.List;
import java.util.Objects;
import com.sigpwned.oauth4j.core.annotation.Generated;

public class OAuthHttpRequest {
  public static final String POST_METHOD = "POST";

  public static OAuthHttpRequest of(String method, String url,
      List<OAuthQueryParameter> queryParameters, List<OAuthHttpHeader> headers,
      List<OAuthFormParameter> formParameters) {
    return new OAuthHttpRequest(method, url, headers, queryParameters, formParameters);
  }

  private final String method;

  /**
   * The absolute URL, including scheme and hostname. The scheme and hostname should be lowercase.
   */
  private final String url;

  private final List<OAuthHttpHeader> headers;

  private final List<OAuthQueryParameter> queryParameters;

  private final List<OAuthFormParameter> formParameters;

  public OAuthHttpRequest(String method, String url, List<OAuthHttpHeader> headers,
      List<OAuthQueryParameter> queryParameters, List<OAuthFormParameter> formParameters) {
    if (method == null)
      throw new NullPointerException();
    if (url == null)
      throw new NullPointerException();
    if (headers == null)
      throw new NullPointerException();
    if (queryParameters == null)
      throw new NullPointerException();
    if (formParameters == null)
      throw new NullPointerException();
    this.method = method;
    this.url = url;
    this.headers = unmodifiableList(headers);
    this.queryParameters = unmodifiableList(queryParameters);
    this.formParameters = unmodifiableList(formParameters);
  }

  /**
   * @return the method
   */
  public String getMethod() {
    return method;
  }

  /**
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * @return the headers
   */
  public List<OAuthHttpHeader> getHeaders() {
    return headers;
  }

  /**
   * @return the queryParameters
   */
  public List<OAuthQueryParameter> getQueryParameters() {
    return queryParameters;
  }

  /**
   * @return the formParameters
   */
  public List<OAuthFormParameter> getFormParameters() {
    return formParameters;
  }

  @Override
  @Generated
  public int hashCode() {
    return Objects.hash(formParameters, headers, method, queryParameters, url);
  }

  @Override
  @Generated
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    OAuthHttpRequest other = (OAuthHttpRequest) obj;
    return Objects.equals(formParameters, other.formParameters)
        && Objects.equals(headers, other.headers) && Objects.equals(method, other.method)
        && Objects.equals(queryParameters, other.queryParameters) && Objects.equals(url, other.url);
  }

  @Override
  @Generated
  public String toString() {
    final int maxLen = 10;
    return "OAuthHttpRequest [method=" + method + ", url=" + url + ", headers="
        + (headers != null ? headers.subList(0, Math.min(headers.size(), maxLen)) : null)
        + ", queryParameters="
        + (queryParameters != null
            ? queryParameters.subList(0, Math.min(queryParameters.size(), maxLen))
            : null)
        + ", formParameters="
        + (formParameters != null
            ? formParameters.subList(0, Math.min(formParameters.size(), maxLen))
            : null)
        + "]";
  }
}
