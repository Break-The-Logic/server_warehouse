package com.greateastern.warehouse.common.api;

public record ExternalApiFailureMessage(
    String originalCode,
    String originalMessage
) implements ApiMessage {

  public ExternalApiFailureMessage {
    if (originalCode == null || originalCode.isBlank()) {
      throw new IllegalArgumentException("External failure message 'originalCode' must not be blank");
    }

    if (originalMessage == null || originalMessage.isBlank()) {
      throw new IllegalArgumentException("External failure message 'originalMessage' must not be blank");
    }
  }
}
