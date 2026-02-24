package com.greateastern.warehouse.variant.service;

import com.greateastern.warehouse.common.error.ConflictException;
import com.greateastern.warehouse.common.error.ResourceNotFoundException;
import com.greateastern.warehouse.item.domain.Item;
import com.greateastern.warehouse.item.service.ItemService;
import com.greateastern.warehouse.variant.api.dto.CreateVariantRequest;
import com.greateastern.warehouse.variant.api.dto.UpdateVariantRequest;
import com.greateastern.warehouse.variant.api.dto.VariantResponse;
import com.greateastern.warehouse.variant.domain.ItemVariant;
import com.greateastern.warehouse.variant.domain.ItemVariantRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class VariantService {

  private final ItemService itemService;
  private final ItemVariantRepository itemVariantRepository;

  public VariantService(ItemService itemService, ItemVariantRepository itemVariantRepository) {
    this.itemService = itemService;
    this.itemVariantRepository = itemVariantRepository;
  }

  @Transactional
  public VariantResponse create(Long itemId, CreateVariantRequest request) {
    Item item = itemService.getItemEntity(itemId);
    validateSkuCreation(request.sku().trim());
    ItemVariant variant = new ItemVariant(
        request.sku().trim(),
        request.name().trim(),
        request.price(),
        request.stockQuantity(),
        toActive(request.active())
    );
    item.addVariant(variant);
    ItemVariant savedVariant = itemVariantRepository.save(variant);
    return toResponse(savedVariant);
  }

  public List<VariantResponse> findByItemId(Long itemId, boolean activeOnly) {
    itemService.getItemEntity(itemId);
    List<ItemVariant> variants = activeOnly
        ? itemVariantRepository.findAllByItemIdAndActiveOrderByCreatedAtDesc(itemId, true)
        : itemVariantRepository.findAllByItemIdOrderByCreatedAtDesc(itemId);
    return variants
        .stream()
        .map(this::toResponse)
        .toList();
  }

  public VariantResponse findById(Long variantId) {
    return toResponse(getVariantEntity(variantId));
  }

  @Transactional
  public VariantResponse update(Long variantId, UpdateVariantRequest request) {
    ItemVariant variant = getVariantEntity(variantId);
    validateSkuUpdate(request.sku().trim(), variantId);
    variant.update(
        request.sku().trim(),
        request.name().trim(),
        request.price(),
        request.stockQuantity(),
        toActive(request.active())
    );
    return toResponse(variant);
  }

  @Transactional
  public void delete(Long variantId) {
    ItemVariant variant = getVariantEntity(variantId);
    itemVariantRepository.delete(variant);
  }

  public ItemVariant getVariantEntity(Long variantId) {
    return itemVariantRepository.findById(variantId)
        .orElseThrow(() -> new ResourceNotFoundException("Variant with id " + variantId + " was not found"));
  }

  @Transactional
  public List<ItemVariant> lockByIds(Collection<Long> variantIds) {
    return itemVariantRepository.findAllByIdForUpdate(variantIds);
  }

  private VariantResponse toResponse(ItemVariant variant) {
    return new VariantResponse(
        variant.getId(),
        variant.getItem().getId(),
        variant.getSku(),
        variant.getName(),
        variant.getPrice(),
        variant.getStockQuantity(),
        variant.isActive(),
        variant.getCreatedAt(),
        variant.getUpdatedAt()
    );
  }

  private boolean toActive(Boolean active) {
    return active == null || active;
  }

  private void validateSkuCreation(String sku) {
    if (itemVariantRepository.existsBySku(sku)) {
      throw new ConflictException("Variant SKU already exists: " + sku);
    }
  }

  private void validateSkuUpdate(String sku, Long variantId) {
    if (itemVariantRepository.existsBySkuAndIdNot(sku, variantId)) {
      throw new ConflictException("Variant SKU already exists: " + sku);
    }
  }
}
