package com.greateastern.warehouse.common.error;

public class ThirdPartyApiException extends RuntimeException {

  private final String originalCode;
  private final String originalMessage;

  public ThirdPartyApiException(String originalCode, String originalMessage) {
    super("Third-party API call failed");
    this.originalCode = originalCode;
    this.originalMessage = originalMessage;
  }

  public String getOriginalCode() {
    return originalCode;
  }

  public String getOriginalMessage() {
    return originalMessage;
  }
}
