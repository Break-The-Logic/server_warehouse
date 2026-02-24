package com.greateastern.warehouse.item.service;

import com.greateastern.warehouse.common.error.ResourceNotFoundException;
import com.greateastern.warehouse.item.api.dto.CreateItemRequest;
import com.greateastern.warehouse.item.api.dto.ItemResponse;
import com.greateastern.warehouse.item.api.dto.UpdateItemRequest;
import com.greateastern.warehouse.item.domain.Item;
import com.greateastern.warehouse.item.domain.ItemRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ItemService {

  private final ItemRepository itemRepository;

  public ItemService(ItemRepository itemRepository) {
    this.itemRepository = itemRepository;
  }

  @Transactional
  public ItemResponse create(CreateItemRequest request) {
    Item item = new Item(request.name().trim(), request.description().trim(), toActive(request.active()));
    Item savedItem = itemRepository.save(item);
    return toResponse(savedItem);
  }

  public List<ItemResponse> findAll(boolean activeOnly) {
    List<Item> items = activeOnly
        ? itemRepository.findAllByActiveOrderByCreatedAtDesc(true)
        : itemRepository.findAll();
    return items.stream().map(this::toResponse).toList();
  }

  public ItemResponse findById(Long itemId) {
    return toResponse(getItemEntity(itemId));
  }

  @Transactional
  public ItemResponse update(Long itemId, UpdateItemRequest request) {
    Item item = getItemEntity(itemId);
    item.update(request.name().trim(), request.description().trim(), toActive(request.active()));
    return toResponse(item);
  }

  @Transactional
  public void delete(Long itemId) {
    Item item = getItemEntity(itemId);
    itemRepository.delete(item);
  }

  public Item getItemEntity(Long itemId) {
    return itemRepository.findById(itemId)
        .orElseThrow(() -> new ResourceNotFoundException("Item with id " + itemId + " was not found"));
  }

  private ItemResponse toResponse(Item item) {
    return new ItemResponse(
        item.getId(),
        item.getName(),
        item.getDescription(),
        item.isActive(),
        item.getCreatedAt(),
        item.getUpdatedAt(),
        item.getVariants().size()
    );
  }

  private boolean toActive(Boolean active) {
    return active == null || active;
  }
}
