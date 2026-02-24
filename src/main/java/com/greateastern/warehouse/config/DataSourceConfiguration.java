package com.greateastern.warehouse.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class DataSourceConfiguration {

  @Bean
  public DataSource dataSource(Environment environment) {
    String rawUrl = firstNonBlank(
        environment.getProperty("SPRING_DATASOURCE_URL"),
        environment.getProperty("SUPABASE_DATABASE_URL"),
        environment.getProperty("SUPABASE_PROJECT_URL")
    );
    String databaseName = environment.getProperty("SUPABASE_DATABASE_NAME");
    String sslMode = environment.getProperty("SUPABASE_DATABASE_SSLMODE");
    String jdbcUrl = SupabaseJdbcUrlResolver.resolveJdbcUrl(rawUrl, databaseName, sslMode);
    String username = firstNonBlank(
        environment.getProperty("SPRING_DATASOURCE_USERNAME"),
        environment.getProperty("SUPABASE_DATABASE_USERNAME"),
        "postgres"
    );
    String password = firstNonBlankOrNull(
        environment.getProperty("SPRING_DATASOURCE_PASSWORD"),
        environment.getProperty("SUPABASE_DATABASE_PASSWORD")
    );
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(jdbcUrl);
    config.setUsername(username);

    if (password != null) {
      config.setPassword(password);
    }

    config.setDriverClassName("org.postgresql.Driver");
    config.setMaximumPoolSize(resolveInteger(environment, "DATABASE_POOL_MAX_SIZE", 10));
    config.setMinimumIdle(resolveInteger(environment, "DATABASE_POOL_MIN_IDLE", 2));
    config.setConnectionTimeout(resolveLong(environment, "DATABASE_POOL_CONNECTION_TIMEOUT_MS", 30000L));
    return new HikariDataSource(config);
  }

  private int resolveInteger(Environment environment, String key, int defaultValue) {
    String rawValue = environment.getProperty(key);

    if (rawValue == null || rawValue.isBlank()) {
      return defaultValue;
    }

    try {
      return Integer.parseInt(rawValue.trim());
    } catch (NumberFormatException ex) {
      throw new IllegalStateException("Invalid integer value for " + key + ": " + rawValue);
    }
  }

  private long resolveLong(Environment environment, String key, long defaultValue) {
    String rawValue = environment.getProperty(key);

    if (rawValue == null || rawValue.isBlank()) {
      return defaultValue;
    }

    try {
      return Long.parseLong(rawValue.trim());
    } catch (NumberFormatException ex) {
      throw new IllegalStateException("Invalid long value for " + key + ": " + rawValue);
    }
  }

  private String firstNonBlank(String... values) {
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value.trim();
      }
    }

    throw new IllegalStateException(
        "Database configuration is incomplete. Set SUPABASE_DATABASE_URL (or SUPABASE_PROJECT_URL).");
  }

  private String firstNonBlankOrNull(String... values) {
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value.trim();
      }
    }

    return null;
  }
}
