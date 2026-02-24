package com.greateastern.warehouse.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

  @Bean
  public OpenAPI warehouseOpenApi() {
    return new OpenAPI().info(new Info()
        .title("Warehouse Management API")
        .description("REST API for item, variant, inventory, and sales operations")
        .version("v1")
        .contact(new Contact().name("Engineering Team").email("engineering@example.com")));
  }
}
