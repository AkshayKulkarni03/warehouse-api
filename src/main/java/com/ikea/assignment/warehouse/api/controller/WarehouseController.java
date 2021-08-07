package com.ikea.assignment.warehouse.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ikea.assignment.warehouse.api.exception.FileFormatNotSupportedException;
import com.ikea.assignment.warehouse.api.exception.JsonFileProcessingException;
import com.ikea.assignment.warehouse.api.mapper.InventoryMapper;
import com.ikea.assignment.warehouse.api.mapper.ProductMapper;
import com.ikea.assignment.warehouse.api.model.Inventories;
import com.ikea.assignment.warehouse.api.model.Product;
import com.ikea.assignment.warehouse.api.model.Products;
import com.ikea.assignment.warehouse.service.WarehouseService;
import com.ikea.assignment.warehouse.service.entity.Inventory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@RequestMapping(path = "/api/warehouses")
@Slf4j
public class WarehouseController {

    private final WarehouseService warehouseService;

    private final InventoryMapper inventoryMapper;
    private final ProductMapper productMapper;

    @PostMapping(path = "/inventory", consumes = {MULTIPART_FORM_DATA_VALUE}, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> loadInventory(@RequestParam("file") MultipartFile file) {
        String contentType = file.getContentType();
        if ("application/json".equalsIgnoreCase(contentType)) {
            try (InputStream inputStream = file.getInputStream()) {
                ObjectMapper objectMapper = new ObjectMapper();
                Inventories inventories = objectMapper.readValue(inputStream, Inventories.class);
                if (!CollectionUtils.isEmpty(inventories.getInventory())) {
                    List<Inventory> processedInventories = inventories.getInventory().stream().map(inventory -> warehouseService.storeInventory(inventoryMapper.mapToEntity(inventory))).collect(Collectors.toList());

                    long newCount = processedInventories.stream().filter(inventory -> inventory.getCreatedAt().isEqual(inventory.getModifiedAt())).count();

                    return ResponseEntity.ok(String.format("There were %d imported inventory records. (%d were new, %d were updated)",
                            processedInventories.size(), newCount, (processedInventories.size() - newCount)));
                } else {
                    return ResponseEntity.ok(String.format("There were %d imported inventory records", 0));
                }
            } catch (IOException e) {
                log.error("Json Parsing Exception for Inventory - ", e);
                throw new JsonFileProcessingException(String.format("Input File '%s' for Inventory is not supported", file.getOriginalFilename()));
            }
        } else {
            log.error("File Format {} is not supported", contentType);
            throw new FileFormatNotSupportedException(String.format("File format '%s' is not supported", contentType));
        }
    }

    @PostMapping(path = "/products", consumes = {MULTIPART_FORM_DATA_VALUE}, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> loadProducts(@RequestParam("file") MultipartFile file) {
        String contentType = file.getContentType();
        if ("application/json".equalsIgnoreCase(contentType)) {
            try (InputStream inputStream = file.getInputStream()) {
                ObjectMapper objectMapper = new ObjectMapper();
                Products products = objectMapper.readValue(inputStream, Products.class);
                if (!CollectionUtils.isEmpty(products.getListOfProducts())) {
                    List<com.ikea.assignment.warehouse.service.entity.Product> processedProducts = products.getListOfProducts().stream().map(product -> warehouseService.storeProduct(productMapper.mapToEntity(product))).collect(Collectors.toList());

                    long newCount = processedProducts.stream().filter(product -> product.getCreatedAt().isEqual(product.getModifiedAt())).count();

                    return ResponseEntity.ok(String.format("There were %d imported products. (%d were new, %d were updated)",
                            processedProducts.size(), newCount, (processedProducts.size() - newCount)));
                } else {
                    return ResponseEntity.ok(String.format("There were %d imported products", 0));
                }
            } catch (IOException e) {
                log.error("Json Parsing Exception for Products - ", e);
                throw new JsonFileProcessingException(String.format("Input File '%s' for Products is not supported", file.getOriginalFilename()));
            }
        } else {
            log.error("File Format {} is not supported", contentType);
            throw new FileFormatNotSupportedException(String.format("File format '%s' is not supported", contentType));
        }
    }

    @GetMapping(path = "/products")
    public ResponseEntity<Products> getAllProducts() {
        Map<com.ikea.assignment.warehouse.service.entity.Product, Integer> productIntegerMap = warehouseService.loadAllProducts();
        Products products = getProducts(productIntegerMap);
        return ResponseEntity.ok(products);
    }

    @PutMapping(path = "/product/{id}/quantity/{amount}")
    public ResponseEntity<Product> sellProduct(@PathVariable(value = "id") String id, @PathVariable(value = "amount") Integer amount) {
        AbstractMap.SimpleEntry<com.ikea.assignment.warehouse.service.entity.Product, Integer> productDetails = warehouseService.sellProduct(UUID.fromString(id), amount);
        Product product = productMapper.mapToModel(productDetails.getKey());
        product.setQuantity(productDetails.getValue());
        product.getArticles().clear();
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/product/{id}")
    public ResponseEntity<Products> deleteProduct(@PathVariable(value = "id") String id) {
        Map<com.ikea.assignment.warehouse.service.entity.Product, Integer> productIntegerMap = warehouseService.deleteProduct(UUID.fromString(id));
        Products products = getProducts(productIntegerMap);
        return ResponseEntity.ok(products);
    }

    private Products getProducts(Map<com.ikea.assignment.warehouse.service.entity.Product, Integer> productIntegerMap) {
        Products products = new Products();
        productIntegerMap.forEach((key, value) -> {
            Product product = productMapper.mapToModel(key);
            product.setQuantity(value);
            product.getArticles().clear();
            products.getListOfProducts().add(product);
        });
        return products;
    }
}
