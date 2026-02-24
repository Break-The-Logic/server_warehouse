package com.greateastern.warehouse.item.api;

import com.greateastern.warehouse.common.api.ApiResponse;
import com.greateastern.warehouse.common.api.ApiResponses;
import com.greateastern.warehouse.item.api.dto.CreateItemRequest;
import com.greateastern.warehouse.item.api.dto.ItemResponse;
import com.greateastern.warehouse.item.api.dto.UpdateItemRequest;
import com.greateastern.warehouse.item.service.ItemService;
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
@RequestMapping("/api/items")
public class ItemController {

  private static final String CREATE_ITEM_EXAMPLE = """
      {
        \"name\": \"Migration Demo Item\",
        \"description\": \"Item created from Swagger using default request body\",
        \"active\": true
      }
      """;

  private static final String UPDATE_ITEM_EXAMPLE = """
      {
        \"name\": \"Migration Seed Item A Updated\",
        \"description\": \"Updated item description from Swagger default payload\",
        \"active\": true
      }
      """;

  private static final String ITEM_SINGLE_SUCCESS_EXAMPLE = """
      {
        \"code\": \"00\",
        \"message\": \"Item retrieved successfully\",
        \"data\": {
          \"id\": 1001,
          \"name\": \"Migration Seed Item A\",
          \"description\": \"Primary seeded item for immediate API testing\",
          \"active\": true,
          \"createdAt\": \"2026-02-24T07:00:00Z\",
          \"updatedAt\": \"2026-02-24T07:00:00Z\",
          \"variantCount\": 2
        }
      }
      """;

  private static final String ITEM_CREATE_SUCCESS_EXAMPLE = """
      {
        \"code\": \"00\",
        \"message\": \"Item created successfully\",
        \"data\": {
          \"id\": 1003,
          \"name\": \"Migration Demo Item\",
          \"description\": \"Item created from Swagger using default request body\",
          \"active\": true,
          \"createdAt\": \"2026-02-24T07:05:00Z\",
          \"updatedAt\": \"2026-02-24T07:05:00Z\",
          \"variantCount\": 0
        }
      }
      """;

  private static final String ITEM_UPDATE_SUCCESS_EXAMPLE = """
      {
        \"code\": \"00\",
        \"message\": \"Item updated successfully\",
        \"data\": {
          \"id\": 1001,
          \"name\": \"Migration Seed Item A Updated\",
          \"description\": \"Updated item description from Swagger default payload\",
          \"active\": true,
          \"createdAt\": \"2026-02-24T07:00:00Z\",
          \"updatedAt\": \"2026-02-24T07:05:00Z\",
          \"variantCount\": 2
        }
      }
      """;

  private static final String ITEM_LIST_SUCCESS_EXAMPLE = """
      {
        \"code\": \"00\",
        \"message\": \"Items retrieved successfully\",
        \"data\": [
          {
            \"id\": 1001,
            \"name\": \"Migration Seed Item A\",
            \"description\": \"Primary seeded item for immediate API testing\",
            \"active\": true,
            \"createdAt\": \"2026-02-24T07:00:00Z\",
            \"updatedAt\": \"2026-02-24T07:00:00Z\",
            \"variantCount\": 2
          }
        ]
      }
      """;

  private static final String ITEM_DELETE_SUCCESS_EXAMPLE = """
      {
        \"code\": \"00\",
        \"message\": \"Item deleted successfully\"
      }
      """;

  private final ItemService itemService;

  public ItemController(ItemService itemService) {
    this.itemService = itemService;
  }

  @PostMapping
  @Operation(
      summary = "Create item",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          required = true,
          content = @Content(mediaType = "application/json", examples = @ExampleObject(value = CREATE_ITEM_EXAMPLE))
      ),
      responses = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(
              responseCode = "201",
              description = "Item created",
              content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ITEM_CREATE_SUCCESS_EXAMPLE))
          )
      }
  )
  public ResponseEntity<ApiResponse<ItemResponse>> create(@Valid @RequestBody CreateItemRequest request) {
    ItemResponse response = itemService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponses.success("Item created successfully", response));
  }

  @GetMapping
  @Operation(
      summary = "List all items",
      parameters = {
          @Parameter(
              name = "activeOnly",
              description = "When true, returns only active items",
              schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "true"),
              example = "true"
          )
      },
      responses = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(
              responseCode = "200",
              description = "Items retrieved",
              content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ITEM_LIST_SUCCESS_EXAMPLE))
          )
      }
  )
  public ResponseEntity<ApiResponse<List<ItemResponse>>> findAll(@RequestParam(defaultValue = "true") boolean activeOnly) {
    List<ItemResponse> response = itemService.findAll(activeOnly);
    return ResponseEntity.ok(ApiResponses.success("Items retrieved successfully", response));
  }

  @GetMapping("/{itemId}")
  @Operation(
      summary = "Get item by id",
      responses = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(
              responseCode = "200",
              description = "Item retrieved",
              content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ITEM_SINGLE_SUCCESS_EXAMPLE))
          )
      }
  )
  public ResponseEntity<ApiResponse<ItemResponse>> findById(
      @Parameter(example = "1001", schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "1001")) @PathVariable Long itemId
  ) {
    ItemResponse response = itemService.findById(itemId);
    return ResponseEntity.ok(ApiResponses.success("Item retrieved successfully", response));
  }

  @PutMapping("/{itemId}")
  @Operation(
      summary = "Update item",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          required = true,
          content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UPDATE_ITEM_EXAMPLE))
      ),
      responses = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(
              responseCode = "200",
              description = "Item updated",
              content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ITEM_UPDATE_SUCCESS_EXAMPLE))
          )
      }
  )
  public ResponseEntity<ApiResponse<ItemResponse>> update(
      @Parameter(example = "1001", schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "1001")) @PathVariable Long itemId,
      @Valid @RequestBody UpdateItemRequest request
  ) {
    ItemResponse response = itemService.update(itemId, request);
    return ResponseEntity.ok(ApiResponses.success("Item updated successfully", response));
  }

  @DeleteMapping("/{itemId}")
  @Operation(
      summary = "Delete item",
      responses = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(
              responseCode = "200",
              description = "Item deleted",
              content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ITEM_DELETE_SUCCESS_EXAMPLE))
          )
      }
  )
  public ResponseEntity<ApiResponse<String>> delete(
      @Parameter(example = "1002", schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "1002")) @PathVariable Long itemId
  ) {
    itemService.delete(itemId);
    return ResponseEntity.ok(ApiResponses.success("Item deleted successfully"));
  }
}
