package com.greateastern.warehouse.variant.api;

import com.greateastern.warehouse.common.api.ApiResponse;
import com.greateastern.warehouse.common.api.ApiResponses;
import com.greateastern.warehouse.variant.api.dto.CreateVariantRequest;
import com.greateastern.warehouse.variant.api.dto.UpdateVariantRequest;
import com.greateastern.warehouse.variant.api.dto.VariantResponse;
import com.greateastern.warehouse.variant.service.VariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class VariantController {

  private static final String CREATE_VARIANT_EXAMPLE = """
      {
        \"sku\": \"MIG-DEMO-NEW-44\",
        \"name\": \"Migration Demo Variant 44\",
        \"price\": 149.90,
        \"stockQuantity\": 15,
        \"active\": true
      }
      """;

  private static final String UPDATE_VARIANT_EXAMPLE = """
      {
        \"sku\": \"MIG-SEED-A-RED-42\",
        \"name\": \"Seed Variant A Red 42 Updated\",
        \"price\": 131.90,
        \"stockQuantity\": 45,
        \"active\": true
      }
      """;

  private static final String VARIANT_SINGLE_SUCCESS_EXAMPLE = """
      {
        \"code\": \"00\",
        \"message\": \"Variant retrieved successfully\",
        \"data\": {
          \"id\": 2001,
          \"itemId\": 1001,
          \"sku\": \"MIG-SEED-A-RED-42\",
          \"name\": \"Seed Variant A Red 42\",
          \"price\": 129.90,
          \"stockQuantity\": 48,
          \"active\": true,
          \"createdAt\": \"2026-02-24T07:00:00Z\",
          \"updatedAt\": \"2026-02-24T07:00:00Z\"
        }
      }
      """;

  private static final String VARIANT_CREATE_SUCCESS_EXAMPLE = """
      {
        \"code\": \"00\",
        \"message\": \"Variant created successfully\",
        \"data\": {
          \"id\": 2003,
          \"itemId\": 1001,
          \"sku\": \"MIG-DEMO-NEW-44\",
          \"name\": \"Migration Demo Variant 44\",
          \"price\": 149.90,
          \"stockQuantity\": 15,
          \"active\": true,
          \"createdAt\": \"2026-02-24T07:05:00Z\",
          \"updatedAt\": \"2026-02-24T07:05:00Z\"
        }
      }
      """;

  private static final String VARIANT_UPDATE_SUCCESS_EXAMPLE = """
      {
        \"code\": \"00\",
        \"message\": \"Variant updated successfully\",
        \"data\": {
          \"id\": 2001,
          \"itemId\": 1001,
          \"sku\": \"MIG-SEED-A-RED-42\",
          \"name\": \"Seed Variant A Red 42 Updated\",
          \"price\": 131.90,
          \"stockQuantity\": 45,
          \"active\": true,
          \"createdAt\": \"2026-02-24T07:00:00Z\",
          \"updatedAt\": \"2026-02-24T07:05:00Z\"
        }
      }
      """;

  private static final String VARIANT_LIST_SUCCESS_EXAMPLE = """
      {
        \"code\": \"00\",
        \"message\": \"Variants retrieved successfully\",
        \"data\": [
          {
            \"id\": 2001,
            \"itemId\": 1001,
            \"sku\": \"MIG-SEED-A-RED-42\",
            \"name\": \"Seed Variant A Red 42\",
            \"price\": 129.90,
            \"stockQuantity\": 48,
            \"active\": true,
            \"createdAt\": \"2026-02-24T07:00:00Z\",
            \"updatedAt\": \"2026-02-24T07:00:00Z\"
          }
        ]
      }
      """;

  private static final String VARIANT_DELETE_SUCCESS_EXAMPLE = """
      {
        \"code\": \"00\",
        \"message\": \"Variant deleted successfully\"
      }
      """;

  private final VariantService variantService;

  public VariantController(VariantService variantService) {
    this.variantService = variantService;
  }

  @PostMapping("/items/{itemId}/variants")
  @Operation(
      summary = "Create variant for item",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          required = true,
          content = @Content(mediaType = "application/json", examples = @ExampleObject(value = CREATE_VARIANT_EXAMPLE))
      ),
      responses = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(
              responseCode = "201",
              description = "Variant created",
              content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VARIANT_CREATE_SUCCESS_EXAMPLE))
          )
      }
  )
  public ResponseEntity<ApiResponse<VariantResponse>> create(
      @Parameter(example = "1001", schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "1001")) @PathVariable Long itemId,
      @Valid @RequestBody CreateVariantRequest request
  ) {
    VariantResponse response = variantService.create(itemId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponses.success("Variant created successfully", response));
  }

  @GetMapping("/items/{itemId}/variants")
  @Operation(
      summary = "List variants by item id",
      parameters = {
          @Parameter(
              name = "activeOnly",
              description = "When true, returns only active variants",
              schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "true"),
              example = "true"
          )
      },
      responses = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(
              responseCode = "200",
              description = "Variants retrieved",
              content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VARIANT_LIST_SUCCESS_EXAMPLE))
          )
      }
  )
  public ResponseEntity<ApiResponse<List<VariantResponse>>> findByItemId(
      @Parameter(example = "1001", schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "1001")) @PathVariable Long itemId,
      @RequestParam(defaultValue = "true") boolean activeOnly
  ) {
    List<VariantResponse> response = variantService.findByItemId(itemId, activeOnly);
    return ResponseEntity.ok(ApiResponses.success("Variants retrieved successfully", response));
  }

  @GetMapping("/variants/{variantId}")
  @Operation(
      summary = "Get variant by id",
      responses = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(
              responseCode = "200",
              description = "Variant retrieved",
              content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VARIANT_SINGLE_SUCCESS_EXAMPLE))
          )
      }
  )
  public ResponseEntity<ApiResponse<VariantResponse>> findById(
      @Parameter(example = "2001", schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "2001")) @PathVariable Long variantId
  ) {
    VariantResponse response = variantService.findById(variantId);
    return ResponseEntity.ok(ApiResponses.success("Variant retrieved successfully", response));
  }

  @PutMapping("/variants/{variantId}")
  @Operation(
      summary = "Update variant",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          required = true,
          content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UPDATE_VARIANT_EXAMPLE))
      ),
      responses = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(
              responseCode = "200",
              description = "Variant updated",
              content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VARIANT_UPDATE_SUCCESS_EXAMPLE))
          )
      }
  )
  public ResponseEntity<ApiResponse<VariantResponse>> update(
      @Parameter(example = "2001", schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "2001")) @PathVariable Long variantId,
      @Valid @RequestBody UpdateVariantRequest request
  ) {
    VariantResponse response = variantService.update(variantId, request);
    return ResponseEntity.ok(ApiResponses.success("Variant updated successfully", response));
  }

  @DeleteMapping("/variants/{variantId}")
  @Operation(
      summary = "Delete variant",
      responses = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(
              responseCode = "200",
              description = "Variant deleted",
              content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VARIANT_DELETE_SUCCESS_EXAMPLE))
          )
      }
  )
  public ResponseEntity<ApiResponse<String>> delete(
      @Parameter(example = "2002", schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "2002")) @PathVariable Long variantId
  ) {
    variantService.delete(variantId);
    return ResponseEntity.ok(ApiResponses.success("Variant deleted successfully"));
  }
}
