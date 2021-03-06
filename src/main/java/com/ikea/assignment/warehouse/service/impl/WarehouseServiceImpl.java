package com.ikea.assignment.warehouse.service.impl;

import com.ikea.assignment.warehouse.api.exception.InventoryMissingException;
import com.ikea.assignment.warehouse.api.exception.ProductNotAvailableException;
import com.ikea.assignment.warehouse.service.WarehouseService;
import com.ikea.assignment.warehouse.service.entity.Article;
import com.ikea.assignment.warehouse.service.entity.Inventory;
import com.ikea.assignment.warehouse.service.entity.Product;
import com.ikea.assignment.warehouse.service.repository.InventoryRepository;
import com.ikea.assignment.warehouse.service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@EnableTransactionManagement
public class WarehouseServiceImpl implements WarehouseService {

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
        final Map<String, Long> articleAndStock = getInventoryStockData();

        List<Product> productList = productRepository.findAll();

        Map<Product, Integer> productsWithStock = new HashMap<>();

        productList.forEach(product -> {
            AbstractMap.SimpleEntry<Product, Integer> productQuantity = getProductQuantity(articleAndStock, product);
            productsWithStock.put(productQuantity.getKey(), productQuantity.getValue());
        });
        return productsWithStock;
    }

    @Override
    public AbstractMap.SimpleEntry<Product, Integer> sellProduct(UUID id, Integer amount) {
        Optional<Product> optionalProduct = productRepository.findById(id);

        Product product = optionalProduct.orElseThrow(() -> new ProductNotAvailableException("Product is not found"));

        AbstractMap.SimpleEntry<Product, Integer> productQuantity = getProductQuantity(getInventoryStockData(), product);
        if (productQuantity.getValue() >= amount) {
            List<Article> articles = product.getArticles();
            articles.forEach(article -> {
                String articleId = article.getInventory().getArticleId();
                Optional<Inventory> optionalInventory = inventoryRepository.findByArticleId(articleId);
                if (optionalInventory.isPresent()) {
                    Inventory inventory = optionalInventory.get();
                    inventory.setStock(inventory.getStock() - article.getAmount());
                    inventoryRepository.save(inventory);
                }
            });
            return getProductQuantity(getInventoryStockData(), product);
        } else {
            throw new ProductNotAvailableException(String.format("Product '%s' is not available in stock with quantity '%d'", product.getName(), amount));
        }
    }

    @Override
    public Map<Product, Integer> deleteProduct(UUID id) {
        Optional<Product> optionalProduct = productRepository.findById(id);

        Product product = optionalProduct.orElseThrow(() -> new ProductNotAvailableException("Product is not found"));

        productRepository.delete(product);

        return loadAllProducts();
    }

    private Map<String, Long> getInventoryStockData() {
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
        return articleAndStock;
    }

    private AbstractMap.SimpleEntry<Product, Integer> getProductQuantity(Map<String, Long> articleAndStock, Product product) {
        Map<String, Long> productRequiredInventory = product.getArticles().stream().collect(Collectors.toMap(article -> article.getInventory().getArticleId(), Article::getAmount));
        long countOfInventoryItemsPresent = productRequiredInventory.entrySet().stream().filter(entry -> articleAndStock.get(entry.getKey()) >= entry.getValue()).count();
        if (countOfInventoryItemsPresent != productRequiredInventory.size()) {
            return new AbstractMap.SimpleEntry<>(product, 0);
        } else {
            List<Integer> collectedItems = productRequiredInventory.keySet().stream().map(key -> Math.toIntExact(articleAndStock.get(key) / productRequiredInventory.get(key))).collect(Collectors.toList());
            return new AbstractMap.SimpleEntry<>(product, Collections.min(collectedItems));
        }
    }
}
