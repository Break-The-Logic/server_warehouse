package org.springframework.web.accept;

import java.util.ArrayList;
import java.util.List;

/**
 * Compatibility bridge for Springdoc compiled against a newer Spring Web API.
 */
public class DefaultApiVersionStrategy implements ApiVersionStrategy {

  @SuppressWarnings("unused")
  private final List<ApiVersionResolver> versionResolvers = new ArrayList<>();

  private Object defaultVersion;

  public Object getDefaultVersion() {
    return defaultVersion;
  }
}
