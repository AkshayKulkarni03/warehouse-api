package com.ikea.assignment.warehouse.api.mapper;

import com.ikea.assignment.warehouse.api.model.Article;
import com.ikea.assignment.warehouse.api.model.Product;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product mapToModel(com.ikea.assignment.warehouse.service.entity.Product product);

    @InheritInverseConfiguration
    com.ikea.assignment.warehouse.service.entity.Product mapToEntity(Product product);

    @Mapping(target = "amountOf", source = "amount")
    @Mapping(target = "articleId", source = "inventory.articleId")
    Article mapArticleToModel(com.ikea.assignment.warehouse.service.entity.Article article);

    @InheritInverseConfiguration
    com.ikea.assignment.warehouse.service.entity.Article mapArticleToEntity(Article article);


}
