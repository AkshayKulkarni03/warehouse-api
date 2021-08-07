package com.ikea.assignment.warehouse.service.impl;

import com.ikea.assignment.warehouse.api.exception.InventoryMissingException;
import com.ikea.assignment.warehouse.service.WareHouseService;
import com.ikea.assignment.warehouse.service.entity.Article;
import com.ikea.assignment.warehouse.service.entity.Inventory;
import com.ikea.assignment.warehouse.service.entity.Product;
import com.ikea.assignment.warehouse.service.repository.InventoryRepository;
import com.ikea.assignment.warehouse.service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Override
    public Map<Product, Integer> loadAllProducts() {
        List<String> distinctArticleIds = productRepository.getDistinctArticleIds();
        final Map<String, Long> articleAndStock = new HashMap<>();
        if (!CollectionUtils.isEmpty(distinctArticleIds)) {
            for (String articleId : distinctArticleIds) {
                Optional<Inventory> optionalInventory = inventoryRepository.findByArticleId(articleId);
                if (optionalInventory.isPresent()) {
                    Long stock = optionalInventory.get().getStock();
                    articleAndStock.put(articleId, stock);
                }
            }
        }
        List<Product> productList = productRepository.findAll();
        Map<Product, Integer> productsWithStock = new HashMap<>();
        for (Product product : productList) {
            Map<String, Long> productRequiredInventory = product.getArticles().stream().collect(Collectors.toMap(article -> article.getInventory().getArticleId(), Article::getAmount));
            long countOfInventoryItemsPresent = productRequiredInventory.entrySet().stream().filter(entry -> articleAndStock.get(entry.getKey()) >= entry.getValue()).count();
            System.out.println("service--" + productRequiredInventory +" ----- "+articleAndStock+ " <--> " + countOfInventoryItemsPresent);
            String key = productRequiredInventory.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();

            productsWithStock.put(product, countOfInventoryItemsPresent == productRequiredInventory.size() ? Math.toIntExact(articleAndStock.get(key) / productRequiredInventory.get(key)) : 0);
        }
        return productsWithStock;
    }
}
