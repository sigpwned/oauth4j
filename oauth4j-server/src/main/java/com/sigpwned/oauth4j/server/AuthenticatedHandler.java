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
package com.sigpwned.oauth4j.server;

import java.io.IOException;
import javax.ws.rs.core.Response;

/**
 * Receives fresh access tokens and returns the HTTP response to send to the newly-authenticated
 * user. A reasonable implementation might store the tokens in a persistent data store and then
 * return a redirect to the main web application.
 */
public interface AuthenticatedHandler {
  public Response authenticated(String accessToken, String accessTokenSecret) throws IOException;
}
