package com.ikea.assignment.warehouse.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Article {

    @JsonProperty("art_id")
    private String articleId;

    @JsonProperty("amount_of")
    private Long amountOf;
}
