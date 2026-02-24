package com.greateastern.warehouse.config;

import java.net.URI;
import java.util.Locale;

public final class SupabaseJdbcUrlResolver {

  private SupabaseJdbcUrlResolver() {
  }

  public static String resolveJdbcUrl(String rawUrl, String databaseName, String sslMode) {
    String normalizedUrl = normalizeRawUrl(rawUrl);
    String normalizedDatabase = normalizeDatabaseName(databaseName);
    String normalizedSslMode = normalizeSslMode(sslMode);

    if (normalizedUrl.startsWith("jdbc:")) {
      return normalizedUrl;
    }

    if (normalizedUrl.startsWith("postgresql://")) {
      return "jdbc:" + normalizedUrl;
    }

    if (normalizedUrl.startsWith("postgres://")) {
      return "jdbc:postgresql://" + normalizedUrl.substring("postgres://".length());
    }

    if (normalizedUrl.startsWith("http://") || normalizedUrl.startsWith("https://")) {
      return toJdbcFromProjectUrl(normalizedUrl, normalizedDatabase, normalizedSslMode);
    }

    throw new IllegalStateException(
        "Unsupported SUPABASE_DATABASE_URL format. Use jdbc:postgresql://..., postgresql://..., or https://<project-ref>.supabase.co");
  }

  private static String toJdbcFromProjectUrl(String projectUrl, String databaseName, String sslMode) {
    URI uri = URI.create(projectUrl);
    String host = uri.getHost();

    if (host == null || host.isBlank()) {
      throw new IllegalStateException("SUPABASE_DATABASE_URL is not a valid URL with host: " + projectUrl);
    }

    String databaseHost = resolveDatabaseHost(host.toLowerCase(Locale.ROOT));
    return "jdbc:postgresql://" + databaseHost + ":5432/" + databaseName + "?sslmode=" + sslMode;
  }

  private static String resolveDatabaseHost(String host) {
    if (host.startsWith("db.") && host.endsWith(".supabase.co")) {
      return host;
    }

    if (host.endsWith(".supabase.co")) {
      return "db." + host;
    }

    return host;
  }

  private static String normalizeRawUrl(String rawUrl) {
    if (rawUrl == null || rawUrl.isBlank()) {
      throw new IllegalStateException(
          "SUPABASE_DATABASE_URL is missing. Set it to jdbc:postgresql://... or https://<project-ref>.supabase.co");
    }

    return rawUrl.trim();
  }

  private static String normalizeDatabaseName(String databaseName) {
    if (databaseName == null || databaseName.isBlank()) {
      return "postgres";
    }

    return databaseName.trim();
  }

  private static String normalizeSslMode(String sslMode) {
    if (sslMode == null || sslMode.isBlank()) {
      return "require";
    }

    return sslMode.trim();
  }
}
