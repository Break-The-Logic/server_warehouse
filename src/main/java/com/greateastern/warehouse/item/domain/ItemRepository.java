package com.greateastern.warehouse.item.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

  List<Item> findAllByActiveOrderByCreatedAtDesc(boolean active);
}
