package com.ikea.assignment.warehouse.api.service.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Article {

    @Id
    private UUID id;
    private Long amount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", referencedColumnName = "articleId")
    private Inventory inventory;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", updatable = false, nullable = false)
    @MapsId
    private Product product;
}
