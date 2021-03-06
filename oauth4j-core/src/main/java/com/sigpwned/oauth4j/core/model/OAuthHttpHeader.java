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
import com.sigpwned.oauth4j.core.annotation.Generated;

public class OAuthHttpHeader {
  public static OAuthHttpHeader of(String key, String value) {
    return new OAuthHttpHeader(key, value);
  }

  /**
   * not encoded
   */
  private final String name;

  /**
   * not encoded
   */
  private final String value;

  public OAuthHttpHeader(String key, String value) {
    this.name = key;
    this.value = value;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
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
    return Objects.hash(name, value);
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
    OAuthHttpHeader other = (OAuthHttpHeader) obj;
    return Objects.equals(name, other.name) && Objects.equals(value, other.value);
  }

  @Override
  @Generated
  public String toString() {
    return "OAuthHttpHeader [key=" + name + ", value=" + value + "]";
  }
}
