package com.greateastern.warehouse.common.api;

import java.util.Optional;

public final class ApiResponses {

  private ApiResponses() {
  }

  public static <T> ApiResponse<T> success(String message, T data) {
    if (data == null) {
      throw new IllegalArgumentException("Success response data must not be null");
    }

    return new ApiResponse<>(ApiCode.SUCCESS.value(), new TextMessage(message), Optional.of(data));
  }

  public static ApiResponse<String> success(String message) {
    return new ApiResponse<>(ApiCode.SUCCESS.value(), new TextMessage(message), Optional.empty());
  }

  public static <T> ApiResponse<T> pending(String message, T data) {
    if (data == null) {
      throw new IllegalArgumentException("Pending response data must not be null");
    }

    return new ApiResponse<>(ApiCode.PENDING.value(), new TextMessage(message), Optional.of(data));
  }

  public static ApiResponse<String> failure(ApiMessage message) {
    return new ApiResponse<>(ApiCode.FAILURE.value(), message, Optional.empty());
  }
}
