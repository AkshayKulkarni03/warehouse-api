package com.ikea.assignment.warehouse.service.impl;

import com.ikea.assignment.warehouse.api.exception.InventoryMissingException;
import com.ikea.assignment.warehouse.service.WareHouseService;
import com.ikea.assignment.warehouse.service.entity.Inventory;
import com.ikea.assignment.warehouse.service.entity.Product;
import com.ikea.assignment.warehouse.service.repository.InventoryRepository;
import com.ikea.assignment.warehouse.service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WareHouseServiceImpl implements WareHouseService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    @Override
    public Inventory storeInventory(Inventory inventory) {
        Optional<Inventory> optionalInventory = inventoryRepository.findByArticleId(inventory.getArticleId());
        Inventory inventory1 = optionalInventory.orElse(inventory);
        if (optionalInventory.isPresent()) {
            inventory1.setStock(inventory.getStock());
        } else {
            inventory1.setCreatedAt(LocalDateTime.now());
        }
        inventory1.setModifiedAt(LocalDateTime.now());
        return inventoryRepository.save(inventory1);
    }

    @Override
    public Product storeProduct(Product product) {
        final Map<Boolean, String> articleCheckMap = new HashMap<>();
        Optional<Product> optionalProduct = productRepository.findByName(product.getName());
        Product productToBeProcessed = optionalProduct.orElse(product);
        if (optionalProduct.isPresent()) {
            productToBeProcessed.getArticles().clear();
            productToBeProcessed.getArticles().addAll(product.getArticles());
        } else {
            productToBeProcessed.setCreatedAt(LocalDateTime.now());
        }
        productToBeProcessed.getArticles().replaceAll(article -> {
            article.setProduct(productToBeProcessed);
            Optional<Inventory> optionalInventory = inventoryRepository.findByArticleId(article.getInventory().getArticleId());
            articleCheckMap.put(optionalInventory.isPresent(), article.getInventory().getArticleId());
            optionalInventory.ifPresent(article::setInventory);
            return article;
        });
        if (articleCheckMap.containsKey(Boolean.FALSE)) {
            throw new InventoryMissingException(String.format("Inventory article with id '%s' is missing", articleCheckMap.get(Boolean.FALSE)));
        }
        productToBeProcessed.setModifiedAt(LocalDateTime.now());
        return productRepository.save(productToBeProcessed);
    }
}
