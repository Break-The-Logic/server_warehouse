package com.greateastern.warehouse.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greateastern.warehouse.config.OpenApiConfiguration;
import com.greateastern.warehouse.config.OpenApiDocsController;
import com.greateastern.warehouse.item.api.ItemController;
import com.greateastern.warehouse.item.service.ItemService;
import com.greateastern.warehouse.sale.api.SaleController;
import com.greateastern.warehouse.sale.service.SaleService;
import com.greateastern.warehouse.variant.api.VariantController;
import com.greateastern.warehouse.variant.service.VariantService;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

class OpenApiDocsContractTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void apiDocsShouldExposeInputsAndSuccessExamplesForAllApis() throws Exception {
    OpenApiDocsController openApiDocsController = createOpenApiDocsController();
    ResponseEntity<String> response = openApiDocsController.apiDocs();
    String body = response.getBody();

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(body).isNotBlank();
    assertThat(body).doesNotContain("\"$ref\":null");

    JsonNode root = objectMapper.readTree(body);

    assertHasQueryParameter(operation(root, "/api/items", "get"), "activeOnly");
    assertHasQueryParameter(operation(root, "/api/items/{itemId}/variants", "get"), "activeOnly");
    assertHasQueryParameter(operation(root, "/api/sales", "get"), "reference");

    assertHasRequestBody(operation(root, "/api/items", "post"));
    assertHasRequestBody(operation(root, "/api/items/{itemId}", "put"));
    assertHasRequestBody(operation(root, "/api/items/{itemId}/variants", "post"));
    assertHasRequestBody(operation(root, "/api/variants/{variantId}", "put"));
    assertHasRequestBody(operation(root, "/api/sales", "post"));

    assertResponseCode(operation(root, "/api/items", "post"), "201", "00");
    assertResponseCode(operation(root, "/api/items", "get"), "200", "00");
    assertResponseCode(operation(root, "/api/items/{itemId}", "get"), "200", "00");
    assertResponseCode(operation(root, "/api/items/{itemId}", "put"), "200", "00");
    assertResponseCode(operation(root, "/api/items/{itemId}", "delete"), "200", "00");
    assertResponseCode(operation(root, "/api/items/{itemId}/variants", "post"), "201", "00");
    assertResponseCode(operation(root, "/api/items/{itemId}/variants", "get"), "200", "00");
    assertResponseCode(operation(root, "/api/variants/{variantId}", "get"), "200", "00");
    assertResponseCode(operation(root, "/api/variants/{variantId}", "put"), "200", "00");
    assertResponseCode(operation(root, "/api/variants/{variantId}", "delete"), "200", "00");
    assertResponseCode(operation(root, "/api/sales", "post"), "201", "00");
    assertResponseCode(operation(root, "/api/sales", "get"), "200", "00");
    assertResponseCode(operation(root, "/api/sales/{saleId}", "get"), "200", "00");
  }

  private OpenApiDocsController createOpenApiDocsController() throws Exception {
    ItemService itemService = new ItemService(null);
    VariantService variantService = new VariantService(itemService, null);
    SaleService saleService = new SaleService(null, variantService);

    GenericApplicationContext context = new GenericApplicationContext();
    context.registerBean("itemController", ItemController.class, () -> new ItemController(itemService));
    context.registerBean("variantController", VariantController.class, () -> new VariantController(variantService));
    context.registerBean("saleController", SaleController.class, () -> new SaleController(saleService));
    context.refresh();

    RequestMappingHandlerMapping requestMappingHandlerMapping = new RequestMappingHandlerMapping();
    requestMappingHandlerMapping.setApplicationContext(context);
    requestMappingHandlerMapping.afterPropertiesSet();

    return new OpenApiDocsController(
        new OpenApiConfiguration().warehouseOpenApi(),
        requestMappingHandlerMapping,
        objectMapper
    );
  }

  private JsonNode operation(JsonNode root, String path, String method) {
    JsonNode operationNode = root.path("paths").path(path).path(method);
    assertThat(operationNode.isMissingNode())
        .as("Missing operation %s %s in generated OpenAPI", method.toUpperCase(), path)
        .isFalse();
    return operationNode;
  }

  private void assertHasQueryParameter(JsonNode operationNode, String expectedName) {
    JsonNode parameters = operationNode.path("parameters");
    assertThat(parameters.isArray()).isTrue();

    boolean found = false;
    for (JsonNode parameter : parameters) {
      String name = parameter.path("name").asText();
      String in = parameter.path("in").asText();
      if (expectedName.equals(name) && "query".equals(in)) {
        found = true;
        break;
      }
    }

    assertThat(found)
        .as("Expected query parameter '%s' in operation", expectedName)
        .isTrue();
  }

  private void assertHasRequestBody(JsonNode operationNode) {
    JsonNode exampleNode = operationNode
        .path("requestBody")
        .path("content")
        .path("application/json")
        .path("example");
    assertThat(exampleNode.isMissingNode()).isFalse();
    assertThat(exampleNode.isNull()).isFalse();
  }

  private void assertResponseCode(JsonNode operationNode, String httpStatus, String expectedCode) {
    JsonNode codeNode = operationNode
        .path("responses")
        .path(httpStatus)
        .path("content")
        .path("application/json")
        .path("example")
        .path("code");

    assertThat(codeNode.asText())
        .as("Expected response code %s for HTTP %s", expectedCode, httpStatus)
        .isEqualTo(expectedCode);
  }
}
