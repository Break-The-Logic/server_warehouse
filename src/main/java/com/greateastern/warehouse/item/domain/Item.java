package com.greateastern.warehouse.item.domain;

import com.greateastern.warehouse.common.domain.AuditableEntity;
import com.greateastern.warehouse.variant.domain.ItemVariant;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "items")
public class Item extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(nullable = false, length = 2000)
  private String description;

  @Column(nullable = false)
  private boolean active;

  @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ItemVariant> variants = new ArrayList<>();

  protected Item() {
  }

  public Item(String name, String description, boolean active) {
    this.name = name;
    this.description = description;
    this.active = active;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public boolean isActive() {
    return active;
  }

  public List<ItemVariant> getVariants() {
    return variants;
  }

  public void update(String name, String description, boolean active) {
    this.name = name;
    this.description = description;
    this.active = active;
  }

  public void addVariant(ItemVariant variant) {
    variants.add(variant);
    variant.assignItem(this);
  }

  public void removeVariant(ItemVariant variant) {
    variants.remove(variant);
    variant.assignItem(null);
  }
}
