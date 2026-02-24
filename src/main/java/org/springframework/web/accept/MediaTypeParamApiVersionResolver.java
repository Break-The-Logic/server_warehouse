package org.springframework.web.accept;

import org.springframework.http.MediaType;

/**
 * Compatibility bridge for Springdoc compiled against a newer Spring Web API.
 */
public class MediaTypeParamApiVersionResolver implements ApiVersionResolver {

  @SuppressWarnings("unused")
  private MediaType compatibleMediaType;

  @SuppressWarnings("unused")
  private String parameterName;
}
