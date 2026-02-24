package com.greateastern.warehouse.sale.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {

  Optional<Sale> findByReference(String reference);

  List<Sale> findAllByReferenceOrderByCreatedAtDesc(String reference);

  List<Sale> findAllByOrderByCreatedAtDesc();
}
