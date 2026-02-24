package com.greateastern.warehouse.variant.domain;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemVariantRepository extends JpaRepository<ItemVariant, Long> {

  List<ItemVariant> findAllByItemIdOrderByCreatedAtDesc(Long itemId);

  List<ItemVariant> findAllByItemIdAndActiveOrderByCreatedAtDesc(Long itemId, boolean active);

  boolean existsBySku(String sku);

  boolean existsBySkuAndIdNot(String sku, Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select variant from ItemVariant variant where variant.id in :ids")
  List<ItemVariant> findAllByIdForUpdate(@Param("ids") Collection<Long> ids);
}
