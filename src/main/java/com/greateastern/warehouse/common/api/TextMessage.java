package com.greateastern.warehouse.common.api;

import com.fasterxml.jackson.annotation.JsonValue;

public record TextMessage(String value) implements ApiMessage {

  public TextMessage {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Response message must not be blank");
    }
  }

  @JsonValue
  public String toJsonValue() {
    return value;
  }
}
