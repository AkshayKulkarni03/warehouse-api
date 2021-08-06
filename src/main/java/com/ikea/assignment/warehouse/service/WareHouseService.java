package com.ikea.assignment.warehouse.service;

import com.ikea.assignment.warehouse.service.entity.Inventory;
import com.ikea.assignment.warehouse.service.entity.Product;

public interface WareHouseService {

    Inventory storeInventory(Inventory inventory);
    Product storeProduct(Product product);
}
