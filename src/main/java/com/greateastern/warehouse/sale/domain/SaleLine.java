package com.greateastern.warehouse.sale.domain;

import com.greateastern.warehouse.variant.domain.ItemVariant;
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
@Table(name = "sale_lines")
public class SaleLine {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "sale_id", nullable = false)
  private Sale sale;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "variant_id", nullable = false)
  private ItemVariant variant;

  @Column(nullable = false)
  private Integer quantity;

  @Column(nullable = false, precision = 16, scale = 2)
  private BigDecimal unitPrice;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal lineTotal;

  protected SaleLine() {
  }

  public SaleLine(ItemVariant variant, Integer quantity) {
    this.variant = variant;
    this.quantity = quantity;
    this.unitPrice = variant.getPrice();
    this.lineTotal = variant.getPrice().multiply(BigDecimal.valueOf(quantity));
  }

  public Long getId() {
    return id;
  }

  public Sale getSale() {
    return sale;
  }

  public ItemVariant getVariant() {
    return variant;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public BigDecimal getUnitPrice() {
    return unitPrice;
  }

  public BigDecimal getLineTotal() {
    return lineTotal;
  }

  public void assignSale(Sale sale) {
    this.sale = sale;
  }
}
