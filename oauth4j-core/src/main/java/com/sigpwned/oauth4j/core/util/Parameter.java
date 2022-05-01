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
package com.sigpwned.oauth4j.core.util;

import java.util.Comparator;
import java.util.Objects;

public class Parameter implements Comparable<Parameter> {
  public static Parameter of(String key, String value) {
    return new Parameter(key, value);
  }

  private final String key;

  private final String value;

  public Parameter(String key, String value) {
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
  public int hashCode() {
    return Objects.hash(key, value);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Parameter other = (Parameter) obj;
    return Objects.equals(key, other.key) && Objects.equals(value, other.value);
  }

  @Override
  public String toString() {
    return "Parameter [key=" + key + ", value=" + value + "]";
  }

  private static final Comparator<Parameter> COMPARATOR =
      Comparator.<Parameter, String>comparing(p -> Encodings.urlencode(p.getKey()))
          .<String>thenComparing(p -> Encodings.urlencode(p.getValue()));

  @Override
  public int compareTo(Parameter that) {
    return COMPARATOR.compare(this, that);
  }
}
