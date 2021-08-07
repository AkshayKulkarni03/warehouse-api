package com.ikea.assignment.warehouse.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {

    private UUID id;
    private String name;

    @JsonProperty("contain_articles")
    private List<Article> articles;

    private Integer quantity;
}
