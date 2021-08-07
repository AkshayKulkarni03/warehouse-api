package com.ikea.assignment.warehouse.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Products {

    @JsonProperty("products")
    @Setter(AccessLevel.NONE)
    private List<Product> listOfProducts;

    public List<Product> getListOfProducts() {
        if (this.listOfProducts == null) {
            this.listOfProducts = new ArrayList<>();
        }
        return listOfProducts;
    }
}
