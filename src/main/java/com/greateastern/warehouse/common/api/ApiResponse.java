package com.greateastern.warehouse.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record ApiResponse<T>(
    String code,
    ApiMessage message,
    @JsonInclude(JsonInclude.Include.NON_ABSENT) Optional<T> data
) {

  public ApiResponse {
    if (!ApiCode.isSupported(code)) {
      throw new IllegalArgumentException("Unsupported response code: " + code);
    }

    if (message == null) {
      throw new IllegalArgumentException("Response message must not be null");
    }

    if (data == null) {
      throw new IllegalArgumentException("Response data container must not be null");
    }
  }
}
