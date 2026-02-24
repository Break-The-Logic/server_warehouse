package com.greateastern.warehouse.common.api;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MissingFieldsFailureMessage implements ApiMessage {

  private final Map<String, String> fields;

  public MissingFieldsFailureMessage(Map<String, String> fields) {
    if (fields == null || fields.isEmpty()) {
      throw new IllegalArgumentException("Missing fields map must not be empty");
    }

    Map<String, String> sanitized = new LinkedHashMap<>();

    for (Map.Entry<String, String> entry : fields.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();

      if (key != null && !key.isBlank() && value != null && !value.isBlank()) {
        sanitized.put(key, value);
      }
    }

    if (sanitized.isEmpty()) {
      throw new IllegalArgumentException("Missing fields map must contain at least one valid entry");
    }

    this.fields = Collections.unmodifiableMap(new LinkedHashMap<>(sanitized));
  }

  @JsonAnyGetter
  public Map<String, String> fields() {
    return fields;
  }
}
