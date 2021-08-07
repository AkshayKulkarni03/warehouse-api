package com.ikea.assignment.warehouse.service;

import com.ikea.assignment.warehouse.service.entity.Inventory;
import com.ikea.assignment.warehouse.service.entity.Product;

import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;

public interface WarehouseService {

    Inventory storeInventory(Inventory inventory);

    Product storeProduct(Product product);

    Map<Product, Integer> loadAllProducts();

    AbstractMap.SimpleEntry<Product, Integer> sellProduct(UUID id, Integer amount);

    Map<Product, Integer> deleteProduct(UUID id);
}
