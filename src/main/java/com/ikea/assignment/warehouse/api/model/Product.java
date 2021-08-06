package com.ikea.assignment.warehouse.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Product {

    private String name;

    @JsonProperty("contain_articles")
    private List<Article> articles;
}
