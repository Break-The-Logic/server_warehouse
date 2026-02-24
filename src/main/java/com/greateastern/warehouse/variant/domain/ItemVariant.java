package com.greateastern.warehouse.variant.domain;

import com.greateastern.warehouse.common.domain.AuditableEntity;
import com.greateastern.warehouse.common.error.BusinessRuleException;
import com.greateastern.warehouse.item.domain.Item;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "item_variants")
public class ItemVariant extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "item_id", nullable = false)
  private Item item;

  @Column(nullable = false, unique = true, length = 100)
  private String sku;

  @Column(nullable = false, length = 160)
  private String name;

  @Column(nullable = false, precision = 16, scale = 2)
  private BigDecimal price;

  @Column(nullable = false)
  private Integer stockQuantity;

  @Column(nullable = false)
  private boolean active;

  protected ItemVariant() {
  }

  public ItemVariant(String sku, String name, BigDecimal price, Integer stockQuantity, boolean active) {
    this.sku = sku;
    this.name = name;
    this.price = price;
    this.stockQuantity = stockQuantity;
    this.active = active;
  }

  public Long getId() {
    return id;
  }

  public Item getItem() {
    return item;
  }

  public String getSku() {
    return sku;
  }

  public String getName() {
    return name;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public Integer getStockQuantity() {
    return stockQuantity;
  }

  public boolean isActive() {
    return active;
  }

  public void assignItem(Item item) {
    this.item = item;
  }

  public void update(String sku, String name, BigDecimal price, Integer stockQuantity, boolean active) {
    this.sku = sku;
    this.name = name;
    this.price = price;
    this.stockQuantity = stockQuantity;
    this.active = active;
  }

  public void decreaseStock(int quantity) {
    if (stockQuantity < quantity) {
      throw new BusinessRuleException(
          "Insufficient stock for variant " + id + ". Available: " + stockQuantity + ", requested: " + quantity);
    }

    stockQuantity = stockQuantity - quantity;
  }
}
