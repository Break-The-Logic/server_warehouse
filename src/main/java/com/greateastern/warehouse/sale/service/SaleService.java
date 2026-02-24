package com.greateastern.warehouse.sale.service;

import com.greateastern.warehouse.common.error.BusinessRuleException;
import com.greateastern.warehouse.common.error.ResourceNotFoundException;
import com.greateastern.warehouse.sale.api.dto.CreateSaleLineRequest;
import com.greateastern.warehouse.sale.api.dto.CreateSaleRequest;
import com.greateastern.warehouse.sale.api.dto.SaleLineResponse;
import com.greateastern.warehouse.sale.api.dto.SaleResponse;
import com.greateastern.warehouse.sale.domain.Sale;
import com.greateastern.warehouse.sale.domain.SaleLine;
import com.greateastern.warehouse.sale.domain.SaleRepository;
import com.greateastern.warehouse.variant.domain.ItemVariant;
import com.greateastern.warehouse.variant.service.VariantService;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SaleService {

  private final SaleRepository saleRepository;
  private final VariantService variantService;

  public SaleService(SaleRepository saleRepository, VariantService variantService) {
    this.saleRepository = saleRepository;
    this.variantService = variantService;
  }

  @Transactional
  public SaleResponse create(CreateSaleRequest request) {
    Map<Long, Integer> quantityByVariant = aggregateQuantities(request.lines());
    List<ItemVariant> lockedVariants = variantService.lockByIds(quantityByVariant.keySet())
        .stream()
        .sorted(Comparator.comparing(ItemVariant::getId))
        .toList();
    Map<Long, ItemVariant> variantsById = mapVariantsById(lockedVariants);
    ensureAllVariantsExist(quantityByVariant, variantsById);

    Sale sale = new Sale(resolveReference(request.reference()));

    for (Map.Entry<Long, Integer> entry : quantityByVariant.entrySet()) {
      ItemVariant variant = variantsById.get(entry.getKey());
      Integer quantity = entry.getValue();
      ensureVariantCanBeSold(variant);
      variant.decreaseStock(quantity);
      SaleLine saleLine = new SaleLine(variant, quantity);
      sale.addLine(saleLine);
    }

    Sale savedSale = saleRepository.save(sale);
    return toResponse(savedSale);
  }

  public List<SaleResponse> findAll(String reference) {
    String normalizedReference = reference == null ? "" : reference.trim();
    List<Sale> sales = normalizedReference.isBlank()
        ? saleRepository.findAllByOrderByCreatedAtDesc()
        : saleRepository.findAllByReferenceOrderByCreatedAtDesc(normalizedReference);
    return sales.stream().map(this::toResponse).toList();
  }

  public SaleResponse findById(Long saleId) {
    Sale sale = saleRepository.findById(saleId)
        .orElseThrow(() -> new ResourceNotFoundException("Sale with id " + saleId + " was not found"));
    return toResponse(sale);
  }

  private Map<Long, Integer> aggregateQuantities(List<CreateSaleLineRequest> lines) {
    Map<Long, Integer> quantityByVariant = new LinkedHashMap<>();

    for (CreateSaleLineRequest line : lines) {
      quantityByVariant.merge(line.variantId(), line.quantity(), Integer::sum);
    }

    return quantityByVariant;
  }

  private Map<Long, ItemVariant> mapVariantsById(List<ItemVariant> variants) {
    Map<Long, ItemVariant> variantsById = new LinkedHashMap<>();

    for (ItemVariant variant : variants) {
      variantsById.put(variant.getId(), variant);
    }

    return variantsById;
  }

  private void ensureAllVariantsExist(Map<Long, Integer> quantityByVariant, Map<Long, ItemVariant> variantsById) {
    for (Long variantId : quantityByVariant.keySet()) {
      if (!variantsById.containsKey(variantId)) {
        throw new ResourceNotFoundException("Variant with id " + variantId + " was not found");
      }
    }
  }

  private String resolveReference(String reference) {
    String candidate = reference == null ? "" : reference.trim();

    if (candidate.isBlank()) {
      return "SALE-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    saleRepository.findByReference(candidate).ifPresent(existing -> {
      throw new BusinessRuleException("Sale reference already exists: " + candidate);
    });

    return candidate;
  }

  private void ensureVariantCanBeSold(ItemVariant variant) {
    if (!variant.isActive()) {
      throw new BusinessRuleException("Variant " + variant.getId() + " is inactive and cannot be sold");
    }

    if (!variant.getItem().isActive()) {
      throw new BusinessRuleException(
          "Item " + variant.getItem().getId() + " is inactive and cannot be sold");
    }
  }

  private SaleResponse toResponse(Sale sale) {
    List<SaleLineResponse> lines = sale.getLines().stream()
        .map(line -> new SaleLineResponse(
            line.getId(),
            line.getVariant().getId(),
            line.getVariant().getSku(),
            line.getVariant().getName(),
            line.getQuantity(),
            line.getUnitPrice(),
            line.getLineTotal()
        ))
        .toList();

    return new SaleResponse(
        sale.getId(),
        sale.getReference(),
        sale.getTotalAmount(),
        sale.getCreatedAt(),
        lines
    );
  }
}
