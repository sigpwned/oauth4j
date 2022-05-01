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

import java.util.Objects;
import java.util.Optional;
import com.sigpwned.oauth4j.core.annotation.Generated;
import com.sigpwned.oauth4j.core.util.Encodings;

public class OAuthQueryParameter {
  public static OAuthQueryParameter fromString(String s) {
    if (s.isEmpty())
      throw new IllegalArgumentException("empty");

    String[] parts = s.split("=", 2);

    String key = Encodings.urldecode(parts[0]);
    String value = parts.length == 1 ? "" : Encodings.urldecode(parts[1]);

    return of(key, value);
  }

  public static OAuthQueryParameter of(String key, String value) {
    return new OAuthQueryParameter(key, value);
  }

  /**
   * not encoded
   */
  private final String key;

  /**
   * not encoded
   */
  private final String value;

  public OAuthQueryParameter(String key, String value) {
    if (key == null)
      throw new NullPointerException();
    if (value == null)
      throw new NullPointerException();
    this.key = key;
    this.value = value;
  }

  /**
   * @return the key
   */
  public String getKey() {
    return key;
  }

  /**
   * @return the value
   */
  public String getValue() {
    return value;
  }

  @Override
  @Generated
  public int hashCode() {
    return Objects.hash(key, value);
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
    OAuthQueryParameter other = (OAuthQueryParameter) obj;
    return Objects.equals(key, other.key) && Objects.equals(value, other.value);
  }

  @Override
  public String toString() {
    return Encodings.urlencode(getKey()) + "="
        + Encodings.urldecode(Optional.ofNullable(getValue()).orElse(""));
  }
}
