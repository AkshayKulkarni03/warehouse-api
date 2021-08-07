package com.ikea.assignment.warehouse.service.repository;

import com.ikea.assignment.warehouse.service.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByArticleId(String articleId);
}
