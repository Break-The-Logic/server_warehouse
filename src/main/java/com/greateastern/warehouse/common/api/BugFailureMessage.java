package com.greateastern.warehouse.common.api;

public record BugFailureMessage(
    String what,
    String why,
    String how
) implements ApiMessage {

  public BugFailureMessage {
    if (what == null || what.isBlank()) {
      throw new IllegalArgumentException("Failure message 'what' must not be blank");
    }

    if (why == null || why.isBlank()) {
      throw new IllegalArgumentException("Failure message 'why' must not be blank");
    }

    if (how == null || how.isBlank()) {
      throw new IllegalArgumentException("Failure message 'how' must not be blank");
    }
  }
}
