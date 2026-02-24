package org.springframework.web.accept;

/**
 * Compatibility bridge for Springdoc compiled against a newer Spring Web API.
 */
public class QueryApiVersionResolver implements ApiVersionResolver {

  @SuppressWarnings("unused")
  private String queryParamName;
}
