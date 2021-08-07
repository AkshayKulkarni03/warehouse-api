package com.ikea.assignment.warehouse.service;

import com.ikea.assignment.warehouse.service.entity.Inventory;
import com.ikea.assignment.warehouse.service.entity.Product;

import java.util.Map;

public interface WareHouseService {

    Inventory storeInventory(Inventory inventory);

    Product storeProduct(Product product);

    Map<Product, Integer> loadAllProducts();
}
