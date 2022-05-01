package com.sigpwned.oauth4j.server;

import java.io.IOException;
import javax.ws.rs.core.Response;

public interface AuthenticatedHandler {
  public Response authenticated(String accessToken, String accessTokenSecret) throws IOException;
}
