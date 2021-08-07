package com.ikea.assignment.warehouse.service.repository;

import com.ikea.assignment.warehouse.service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByName(String name);

    @Query(value = "select DISTINCT(article_id) from article", nativeQuery = true)
    List<String> getDistinctArticleIds();
}
