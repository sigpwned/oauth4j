package com.sigpwned.oauth4j.server;

import java.io.IOException;
import java.util.Optional;

public interface TokenStore {
  public void putTokenSecret(String token, String tokenSecret) throws IOException;

  public Optional<String> getTokenSecret(String token) throws IOException;
}
