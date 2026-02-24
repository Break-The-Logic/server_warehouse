package com.greateastern.warehouse.sale.api;

import com.greateastern.warehouse.common.api.ApiResponse;
import com.greateastern.warehouse.common.api.ApiResponses;
import com.greateastern.warehouse.sale.api.dto.CreateSaleRequest;
import com.greateastern.warehouse.sale.api.dto.SaleResponse;
import com.greateastern.warehouse.sale.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

  private static final String CREATE_SALE_EXAMPLE = """
      {
        \"reference\": \"\",
        \"lines\": [
          {
            \"variantId\": 2001,
            \"quantity\": 1
          }
        ]
      }
      """;

  private static final String SALE_SINGLE_SUCCESS_EXAMPLE = """
      {
        \"code\": \"00\",
        \"message\": \"Sale retrieved successfully\",
        \"data\": {
          \"id\": 3001,
          \"reference\": \"MIG-SEED-SALE-3001\",
          \"totalAmount\": 259.80,
          \"createdAt\": \"2026-02-24T07:05:00Z\",
          \"lines\": [
            {
              \"id\": 4001,
              \"variantId\": 2001,
              \"sku\": \"MIG-SEED-A-RED-42\",
              \"variantName\": \"Seed Variant A Red 42\",
              \"quantity\": 2,
              \"unitPrice\": 129.90,
              \"lineTotal\": 259.80
            }
          ]
        }
      }
      """;

  private static final String SALE_CREATE_SUCCESS_EXAMPLE = """
      {
        \"code\": \"00\",
        \"message\": \"Sale created successfully\",
        \"data\": {
          \"id\": 3002,
          \"reference\": \"SALE-1740396300000-4fa12c7b\",
          \"totalAmount\": 129.90,
          \"createdAt\": \"2026-02-24T07:05:00Z\",
          \"lines\": [
            {
              \"id\": 4002,
              \"variantId\": 2001,
              \"sku\": \"MIG-SEED-A-RED-42\",
              \"variantName\": \"Seed Variant A Red 42\",
              \"quantity\": 1,
              \"unitPrice\": 129.90,
              \"lineTotal\": 129.90
            }
          ]
        }
      }
      """;

  private static final String SALE_LIST_SUCCESS_EXAMPLE = """
      {
        \"code\": \"00\",
        \"message\": \"Sales retrieved successfully\",
        \"data\": [
          {
            \"id\": 3001,
            \"reference\": \"MIG-SEED-SALE-3001\",
            \"totalAmount\": 259.80,
            \"createdAt\": \"2026-02-24T07:05:00Z\",
            \"lines\": [
              {
                \"id\": 4001,
                \"variantId\": 2001,
                \"sku\": \"MIG-SEED-A-RED-42\",
                \"variantName\": \"Seed Variant A Red 42\",
                \"quantity\": 2,
                \"unitPrice\": 129.90,
                \"lineTotal\": 259.80
              }
            ]
          }
        ]
      }
      """;

  private final SaleService saleService;

  public SaleController(SaleService saleService) {
    this.saleService = saleService;
  }

  @PostMapping
  @Operation(
      summary = "Create sale",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          required = true,
          content = @Content(mediaType = "application/json", examples = @ExampleObject(value = CREATE_SALE_EXAMPLE))
      ),
      responses = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(
              responseCode = "201",
              description = "Sale created",
              content = @Content(mediaType = "application/json", examples = @ExampleObject(value = SALE_CREATE_SUCCESS_EXAMPLE))
          )
      }
  )
  public ResponseEntity<ApiResponse<SaleResponse>> create(@Valid @RequestBody CreateSaleRequest request) {
    SaleResponse response = saleService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponses.success("Sale created successfully", response));
  }

  @GetMapping
  @Operation(
      summary = "List all sales",
      parameters = {
          @Parameter(
              name = "reference",
              description = "Optional sale reference filter",
              schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "MIG-SEED-SALE-3001"),
              example = "MIG-SEED-SALE-3001"
          )
      },
      responses = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(
              responseCode = "200",
              description = "Sales retrieved",
              content = @Content(mediaType = "application/json", examples = @ExampleObject(value = SALE_LIST_SUCCESS_EXAMPLE))
          )
      }
  )
  public ResponseEntity<ApiResponse<List<SaleResponse>>> findAll(
      @RequestParam(defaultValue = "MIG-SEED-SALE-3001") String reference
  ) {
    List<SaleResponse> response = saleService.findAll(reference);
    return ResponseEntity.ok(ApiResponses.success("Sales retrieved successfully", response));
  }

  @GetMapping("/{saleId}")
  @Operation(
      summary = "Get sale by id",
      responses = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(
              responseCode = "200",
              description = "Sale retrieved",
              content = @Content(mediaType = "application/json", examples = @ExampleObject(value = SALE_SINGLE_SUCCESS_EXAMPLE))
          )
      }
  )
  public ResponseEntity<ApiResponse<SaleResponse>> findById(
      @Parameter(example = "3001", schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "3001")) @PathVariable Long saleId
  ) {
    SaleResponse response = saleService.findById(saleId);
    return ResponseEntity.ok(ApiResponses.success("Sale retrieved successfully", response));
  }
}
