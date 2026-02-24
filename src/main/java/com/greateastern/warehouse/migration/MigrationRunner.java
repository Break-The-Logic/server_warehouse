package com.greateastern.warehouse.migration;

import com.greateastern.warehouse.config.SupabaseJdbcUrlResolver;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MigrationRunner {

  private MigrationRunner() {
  }

  public static void main(String[] args) throws Exception {
    Path rootPath = resolveRootPath();
    Map<String, String> fileEnv = loadEnvValues(rootPath);
    String rawUrl = getValue(fileEnv, "SUPABASE_DATABASE_URL")
        .or(() -> getValue(fileEnv, "SUPABASE_PROJECT_URL"))
        .orElseThrow(() -> new IllegalStateException("SUPABASE_DATABASE_URL or SUPABASE_PROJECT_URL is required"));
    String databaseName = getValue(fileEnv, "SUPABASE_DATABASE_NAME").orElse("postgres");
    String sslMode = getValue(fileEnv, "SUPABASE_DATABASE_SSLMODE").orElse("require");
    String jdbcUrl = SupabaseJdbcUrlResolver.resolveJdbcUrl(rawUrl, databaseName, sslMode);
    String username = getValue(fileEnv, "SUPABASE_DATABASE_USERNAME").orElse("postgres");
    String password = getValue(fileEnv, "SUPABASE_DATABASE_PASSWORD").orElse("");

    Path migrationsRoot = rootPath.resolve("db").resolve("migrations");
    List<Path> migrationFiles = listMigrationFiles(migrationsRoot);

    if (migrationFiles.isEmpty()) {
      throw new IllegalStateException("No SQL migration files found under " + migrationsRoot);
    }

    Properties properties = new Properties();
    properties.setProperty("user", username);

    if (!password.isBlank()) {
      properties.setProperty("password", password);
    }

    try (Connection connection = DriverManager.getConnection(jdbcUrl, properties)) {
      connection.setAutoCommit(false);
      ensureMigrationTable(connection);

      for (Path migrationFile : migrationFiles) {
        String migrationName = rootPath.relativize(migrationFile).toString().replace("\\", "/");

        if (isAlreadyApplied(connection, migrationName)) {
          System.out.println("Skipping " + migrationName);
          continue;
        }

        System.out.println("Applying " + migrationName);
        String sql = Files.readString(migrationFile, StandardCharsets.UTF_8);

        try (Statement statement = connection.createStatement()) {
          statement.execute(sql);
        }

        markApplied(connection, migrationName);
        connection.commit();
      }
    } catch (Exception ex) {
      throw new IllegalStateException("Migration execution failed", ex);
    }

    System.out.println("All migrations completed successfully");
  }

  private static Path resolveRootPath() {
    String configuredRoot = System.getProperty("warehouse.root", "").trim();

    if (!configuredRoot.isBlank()) {
      return Paths.get(configuredRoot).toAbsolutePath().normalize();
    }

    return Paths.get("").toAbsolutePath().normalize();
  }

  private static Map<String, String> loadEnvValues(Path rootPath) throws IOException {
    List<Path> candidates = List.of(
        rootPath.resolve(".env"),
        rootPath.resolve(".env.local"),
        rootPath.getParent() == null ? rootPath.resolve(".missing-parent") : rootPath.getParent().resolve(".env"),
        rootPath.getParent() == null ? rootPath.resolve(".missing-parent-local") : rootPath.getParent().resolve(".env.local")
    );

    Map<String, String> values = new HashMap<>();

    for (Path candidate : candidates) {
      if (!Files.exists(candidate) || !Files.isRegularFile(candidate)) {
        continue;
      }

      List<String> lines = Files.readAllLines(candidate, StandardCharsets.UTF_8);

      for (String rawLine : lines) {
        String line = rawLine.trim();

        if (line.isEmpty() || !line.contains("=")) {
          continue;
        }

        int separatorIndex = line.indexOf('=');
        String key = line.substring(0, separatorIndex).trim();
        String value = line.substring(separatorIndex + 1).trim();

        if (!key.isEmpty()) {
          values.put(key, value);
        }
      }
    }

    return values;
  }

  private static Optional<String> getValue(Map<String, String> fileEnv, String key) {
    String systemValue = System.getenv(key);

    if (systemValue != null && !systemValue.isBlank()) {
      return Optional.of(systemValue.trim());
    }

    String fileValue = fileEnv.getOrDefault(key, "");

    if (fileValue.isBlank()) {
      return Optional.empty();
    }

    return Optional.of(fileValue);
  }

  private static List<Path> listMigrationFiles(Path migrationsRoot) throws IOException {
    if (!Files.exists(migrationsRoot) || !Files.isDirectory(migrationsRoot)) {
      return List.of();
    }

    try (Stream<Path> fileStream = Files.walk(migrationsRoot)) {
      return fileStream
          .filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().endsWith(".sql"))
          .sorted(Comparator.comparing(path -> path.toAbsolutePath().toString()))
          .collect(Collectors.toCollection(ArrayList::new));
    }
  }

  private static void ensureMigrationTable(Connection connection) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute(
          "CREATE TABLE IF NOT EXISTS public.schema_migrations ("
              + "filename TEXT PRIMARY KEY,"
              + "applied_at TIMESTAMPTZ NOT NULL DEFAULT NOW()"
              + ")"
      );
    }

    connection.commit();
  }

  private static boolean isAlreadyApplied(Connection connection, String migrationName) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(
        "SELECT COUNT(*) FROM public.schema_migrations WHERE filename = ?")) {
      statement.setString(1, migrationName);

      try (ResultSet resultSet = statement.executeQuery()) {
        resultSet.next();
        return resultSet.getInt(1) > 0;
      }
    }
  }

  private static void markApplied(Connection connection, String migrationName) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(
        "INSERT INTO public.schema_migrations (filename) VALUES (?)")) {
      statement.setString(1, migrationName);
      statement.executeUpdate();
    }
  }
}
