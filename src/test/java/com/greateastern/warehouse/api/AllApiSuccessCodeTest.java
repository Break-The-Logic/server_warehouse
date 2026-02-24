package com.greateastern.warehouse.api;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.greateastern.warehouse.common.error.GlobalExceptionHandler;
import com.greateastern.warehouse.item.api.ItemController;
import com.greateastern.warehouse.item.api.dto.CreateItemRequest;
import com.greateastern.warehouse.item.api.dto.ItemResponse;
import com.greateastern.warehouse.item.api.dto.UpdateItemRequest;
import com.greateastern.warehouse.item.service.ItemService;
import com.greateastern.warehouse.sale.api.SaleController;
import com.greateastern.warehouse.sale.api.dto.CreateSaleRequest;
import com.greateastern.warehouse.sale.api.dto.SaleLineResponse;
import com.greateastern.warehouse.sale.api.dto.SaleResponse;
import com.greateastern.warehouse.sale.service.SaleService;
import com.greateastern.warehouse.variant.api.VariantController;
import com.greateastern.warehouse.variant.api.dto.CreateVariantRequest;
import com.greateastern.warehouse.variant.api.dto.UpdateVariantRequest;
import com.greateastern.warehouse.variant.api.dto.VariantResponse;
import com.greateastern.warehouse.variant.service.VariantService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AllApiSuccessCodeTest {

  private static final ItemResponse ITEM_RESPONSE = new ItemResponse(
      1001L,
      "Migration Seed Item A",
      "Primary seeded item",
      true,
      Instant.parse("2026-02-24T07:00:00Z"),
      Instant.parse("2026-02-24T07:00:00Z"),
      2
  );

  private static final VariantResponse VARIANT_RESPONSE = new VariantResponse(
      2001L,
      1001L,
      "MIG-SEED-A-RED-42",
      "Seed Variant A Red 42",
      new BigDecimal("129.90"),
      48,
      true,
      Instant.parse("2026-02-24T07:00:00Z"),
      Instant.parse("2026-02-24T07:00:00Z")
  );

  private static final SaleResponse SALE_RESPONSE = new SaleResponse(
      3001L,
      "MIG-SEED-SALE-3001",
      new BigDecimal("259.80"),
      Instant.parse("2026-02-24T07:05:00Z"),
      List.of(new SaleLineResponse(
          4001L,
          2001L,
          "MIG-SEED-A-RED-42",
          "Seed Variant A Red 42",
          2,
          new BigDecimal("129.90"),
          new BigDecimal("259.80")
      ))
  );

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    ItemController itemController = new ItemController(new StubItemService());
    VariantController variantController = new VariantController(new StubVariantService());
    SaleController saleController = new SaleController(new StubSaleService());

    mockMvc = MockMvcBuilders.standaloneSetup(itemController, variantController, saleController)
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @Test
  void allBusinessApisShouldReturnSuccessCode00() throws Exception {
    mockMvc.perform(post("/api/items")
            .contentType(APPLICATION_JSON)
            .content("""
                {"name":"Demo Item","description":"Demo item description","active":true}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.code").value("00"));

    mockMvc.perform(get("/api/items").param("activeOnly", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("00"));

    mockMvc.perform(get("/api/items/{itemId}", 1001))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("00"));

    mockMvc.perform(put("/api/items/{itemId}", 1001)
            .contentType(APPLICATION_JSON)
            .content("""
                {"name":"Demo Item Updated","description":"Demo item updated","active":true}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("00"));

    mockMvc.perform(delete("/api/items/{itemId}", 1001))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("00"));

    mockMvc.perform(post("/api/items/{itemId}/variants", 1001)
            .contentType(APPLICATION_JSON)
            .content("""
                {"sku":"MIG-DEMO-NEW-44","name":"Demo Variant 44","price":149.90,"stockQuantity":15,"active":true}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.code").value("00"));

    mockMvc.perform(get("/api/items/{itemId}/variants", 1001).param("activeOnly", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("00"));

    mockMvc.perform(get("/api/variants/{variantId}", 2001))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("00"));

    mockMvc.perform(put("/api/variants/{variantId}", 2001)
            .contentType(APPLICATION_JSON)
            .content("""
                {"sku":"MIG-SEED-A-RED-42","name":"Seed Variant A Red 42 Updated","price":131.90,"stockQuantity":45,"active":true}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("00"));

    mockMvc.perform(delete("/api/variants/{variantId}", 2001))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("00"));

    mockMvc.perform(post("/api/sales")
            .contentType(APPLICATION_JSON)
            .content("""
                {"reference":"","lines":[{"variantId":2001,"quantity":1}]}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.code").value("00"));

    mockMvc.perform(get("/api/sales").param("reference", "MIG-SEED-SALE-3001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("00"));

    mockMvc.perform(get("/api/sales/{saleId}", 3001))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("00"));
  }

  private static final class StubItemService extends ItemService {

    private StubItemService() {
      super(null);
    }

    @Override
    public ItemResponse create(CreateItemRequest request) {
      return ITEM_RESPONSE;
    }

    @Override
    public List<ItemResponse> findAll(boolean activeOnly) {
      return List.of(ITEM_RESPONSE);
    }

    @Override
    public ItemResponse findById(Long itemId) {
      return ITEM_RESPONSE;
    }

    @Override
    public ItemResponse update(Long itemId, UpdateItemRequest request) {
      return ITEM_RESPONSE;
    }

    @Override
    public void delete(Long itemId) {
      // no-op
    }
  }

  private static final class StubVariantService extends VariantService {

    private StubVariantService() {
      super(null, null);
    }

    @Override
    public VariantResponse create(Long itemId, CreateVariantRequest request) {
      return VARIANT_RESPONSE;
    }

    @Override
    public List<VariantResponse> findByItemId(Long itemId, boolean activeOnly) {
      return List.of(VARIANT_RESPONSE);
    }

    @Override
    public VariantResponse findById(Long variantId) {
      return VARIANT_RESPONSE;
    }

    @Override
    public VariantResponse update(Long variantId, UpdateVariantRequest request) {
      return VARIANT_RESPONSE;
    }

    @Override
    public void delete(Long variantId) {
      // no-op
    }
  }

  private static final class StubSaleService extends SaleService {

    private StubSaleService() {
      super(null, null);
    }

    @Override
    public SaleResponse create(CreateSaleRequest request) {
      return SALE_RESPONSE;
    }

    @Override
    public List<SaleResponse> findAll(String reference) {
      return List.of(SALE_RESPONSE);
    }

    @Override
    public SaleResponse findById(Long saleId) {
      return SALE_RESPONSE;
    }
  }
}
