package com.greateastern.warehouse.common.api;

public enum ApiCode {
  SUCCESS("00"),
  PENDING("09"),
  FAILURE("99");

  private final String value;

  ApiCode(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }

  public static boolean isSupported(String value) {
    for (ApiCode code : values()) {
      if (code.value.equals(value)) {
        return true;
      }
    }

    return false;
  }
}
