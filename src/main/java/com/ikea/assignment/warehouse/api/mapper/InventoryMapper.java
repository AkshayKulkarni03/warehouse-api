package com.ikea.assignment.warehouse.api.mapper;

import com.ikea.assignment.warehouse.api.model.Inventory;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "articleId", source = "articleId")
    Inventory mapToModel(com.ikea.assignment.warehouse.service.entity.Inventory inventory);

    @InheritInverseConfiguration
    com.ikea.assignment.warehouse.service.entity.Inventory mapToEntity(Inventory inventory);
}
