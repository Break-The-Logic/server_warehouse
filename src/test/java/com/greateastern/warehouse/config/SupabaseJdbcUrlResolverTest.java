package com.greateastern.warehouse.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SupabaseJdbcUrlResolverTest {

  @Test
  void shouldReturnJdbcUrlWhenInputIsAlreadyJdbc() {
    String resolved = SupabaseJdbcUrlResolver.resolveJdbcUrl(
        "jdbc:postgresql://db.project.supabase.co:5432/postgres?sslmode=require",
        null,
        null
    );

    assertThat(resolved).isEqualTo("jdbc:postgresql://db.project.supabase.co:5432/postgres?sslmode=require");
  }

  @Test
  void shouldConvertPostgresqlSchemeToJdbc() {
    String resolved = SupabaseJdbcUrlResolver.resolveJdbcUrl(
        "postgresql://db.project.supabase.co:5432/postgres?sslmode=require",
        null,
        null
    );

    assertThat(resolved).isEqualTo("jdbc:postgresql://db.project.supabase.co:5432/postgres?sslmode=require");
  }

  @Test
  void shouldConvertProjectUrlToJdbcUrl() {
    String resolved = SupabaseJdbcUrlResolver.resolveJdbcUrl(
        "https://bryhbcrkvjswibxapqyi.supabase.co",
        null,
        null
    );

    assertThat(resolved).isEqualTo(
        "jdbc:postgresql://db.bryhbcrkvjswibxapqyi.supabase.co:5432/postgres?sslmode=require");
  }

  @Test
  void shouldThrowForUnsupportedUrlFormat() {
    assertThatThrownBy(() -> SupabaseJdbcUrlResolver.resolveJdbcUrl("file://tmp/db", null, null))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Unsupported SUPABASE_DATABASE_URL format");
  }
}
