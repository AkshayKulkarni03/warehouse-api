package com.ikea.assignment.warehouse.api.controller;

import com.ikea.assignment.warehouse.api.model.Products;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WarehouseControllerTest {

    @Autowired
    private WarehouseController unitToTest;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getAllProductsTest() {
        ResponseEntity<Products> resultEntity = this.restTemplate.getForEntity("http://localhost:" + port + "/api/warehouses/products", Products.class);
        assertThat(resultEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultEntity.getBody()).isNotNull();
        assertThat(resultEntity.getBody().getListOfProducts().size()).isZero();
    }
}
