package com.ikea.assignment.warehouse.service.impl;

import com.ikea.assignment.warehouse.service.WareHouseService;
import com.ikea.assignment.warehouse.service.entity.Inventory;
import com.ikea.assignment.warehouse.service.entity.Product;
import com.ikea.assignment.warehouse.service.repository.InventoryRepository;
import com.ikea.assignment.warehouse.service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WareHouseServiceImpl implements WareHouseService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    @Override
    public Inventory storeInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    @Override
    public Product storeProduct(Product product) {
        return productRepository.save(product);
    }
}
