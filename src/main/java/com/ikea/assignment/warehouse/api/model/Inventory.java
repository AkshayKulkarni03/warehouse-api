package com.ikea.assignment.warehouse.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Inventory {

    @JsonProperty("art_id")
    private String articleId;

    private String name;
    private Long stock;
}
