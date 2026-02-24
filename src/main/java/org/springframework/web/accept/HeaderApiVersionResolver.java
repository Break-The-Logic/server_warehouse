package org.springframework.web.accept;

/**
 * Compatibility bridge for Springdoc compiled against a newer Spring Web API.
 */
public class HeaderApiVersionResolver implements ApiVersionResolver {

  @SuppressWarnings("unused")
  private String headerName;
}
