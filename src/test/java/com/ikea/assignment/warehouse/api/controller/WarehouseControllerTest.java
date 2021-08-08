package com.ikea.assignment.warehouse.api.controller;

import com.ikea.assignment.warehouse.api.exception.handler.ApiError;
import com.ikea.assignment.warehouse.api.model.Products;
import com.ikea.assignment.warehouse.service.entity.Article;
import com.ikea.assignment.warehouse.service.entity.Inventory;
import com.ikea.assignment.warehouse.service.entity.Product;
import com.ikea.assignment.warehouse.service.repository.InventoryRepository;
import com.ikea.assignment.warehouse.service.repository.ProductRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("t")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WarehouseControllerTest {

    @Autowired
    private WarehouseController unitToTest;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @Order(1)
    void getAllProductsTest() {
        insertDataSQL();
        ResponseEntity<Products> resultEntity = this.restTemplate.getForEntity("http://localhost:" + port + "/api/warehouses/products", Products.class);
        assertThat(resultEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultEntity.getBody()).isNotNull();
        assertThat(resultEntity.getBody().getListOfProducts().size()).isEqualTo(2);
    }

    @Test
    @Order(2)
    void loadInventoryTest_FileFormatNotSupported() {
        final ByteArrayResource byteArrayResource = new ByteArrayResource(new String("{\"Text data\"}").getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "inventory.txt";
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.set("file", byteArrayResource);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<ApiError> resultEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/warehouses/inventory", entity, ApiError.class);

        assertThat(resultEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resultEntity.getBody()).isNotNull();
        assertThat(resultEntity.getBody().getMessage()).contains("File format 'text/plain' is not supported");
    }

    @Test
    @Order(3)
    void loadInventoryTest_JsonFileProcessingException() {
        final ByteArrayResource byteArrayResource = new ByteArrayResource(new String("{\"Text data\"}").getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "inventory.json";
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.set("file", byteArrayResource);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<ApiError> resultEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/warehouses/inventory", entity, ApiError.class);

        assertThat(resultEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resultEntity.getBody()).isNotNull();
        assertThat(resultEntity.getBody().getMessage()).contains("Input File 'inventory.json' for Inventory is not supported");
    }

    @Test
    @Order(4)
    void loadInventoryTest_NoInventoryRecordsProcessed() throws IOException {
        final ByteArrayResource byteArrayResource = new ByteArrayResource(getClass().getClassLoader().getResourceAsStream("blank_inventory.json").readAllBytes()) {
            @Override
            public String getFilename() {
                return "blank_inventory.json";
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.set("file", byteArrayResource);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> resultEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/warehouses/inventory", entity, String.class);

        assertThat(resultEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultEntity.getBody()).isNotNull();
        assertThat(resultEntity.getBody()).contains("There were 0 imported inventory records");
    }

    @Test
    @Order(5)
    void loadInventoryTest_Success() throws IOException {
        final ByteArrayResource byteArrayResource = new ByteArrayResource(getClass().getClassLoader().getResourceAsStream("inventory.json").readAllBytes()) {
            @Override
            public String getFilename() {
                return "inventory.json";
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.set("file", byteArrayResource);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> resultEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/warehouses/inventory", entity, String.class);

        assertThat(resultEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultEntity.getBody()).isNotNull();
        assertThat(resultEntity.getBody()).contains("There were 4 imported inventory records");
    }

    @Test
    @Order(6)
    void loadProductTest_JsonFileProcessingException() {

        final ByteArrayResource byteArrayResource = new ByteArrayResource(new String("{\"test data\"}").getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "products.json";
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.set("file", byteArrayResource);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<ApiError> resultEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/warehouses/products", entity, ApiError.class);

        assertThat(resultEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resultEntity.getBody()).isNotNull();
        assertThat(resultEntity.getBody().getMessage()).contains("Input File 'products.json' for Products is not supported");
    }

    @Test
    @Order(7)
    void loadProductTest_FileNotSupportedException() {

        final ByteArrayResource byteArrayResource = new ByteArrayResource(new String("{\"test data\"}").getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "products.json";
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.set("file", byteArrayResource);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<ApiError> resultEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/warehouses/products", entity, ApiError.class);

        assertThat(resultEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resultEntity.getBody()).isNotNull();
        assertThat(resultEntity.getBody().getMessage()).contains("Input File 'products.json' for Products is not supported");
    }

    @Test
    @Order(8)
    void loadProductTest_BlankJsonProcessing() throws IOException {

        final ByteArrayResource byteArrayResource = new ByteArrayResource(getClass().getClassLoader().getResourceAsStream("blank_products.json").readAllBytes()) {
            @Override
            public String getFilename() {
                return "blank_products.json";
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.set("file", byteArrayResource);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> resultEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/warehouses/products", entity, String.class);

        assertThat(resultEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultEntity.getBody()).isNotNull();
        assertThat(resultEntity.getBody()).contains("There were 0 imported products");
    }

    @Test
    @Order(9)
    void loadProductTest_WrongInventoryProcessing() throws IOException {

        final ByteArrayResource byteArrayResource = new ByteArrayResource(getClass().getClassLoader().getResourceAsStream("wrong_inventory_products.json").readAllBytes()) {
            @Override
            public String getFilename() {
                return "wrong_inventory_products.json";
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.set("file", byteArrayResource);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<ApiError> resultEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/warehouses/products", entity, ApiError.class);

        assertThat(resultEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resultEntity.getBody()).isNotNull();
        assertThat(resultEntity.getBody().getMessage()).contains("Inventory article with id '30' is missing");
    }

    @Test
    @Order(10)
    void loadProductTest_Success() throws IOException {

        final ByteArrayResource byteArrayResource = new ByteArrayResource(getClass().getClassLoader().getResourceAsStream("products.json").readAllBytes()) {
            @Override
            public String getFilename() {
                return "products.json";
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.set("file", byteArrayResource);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> resultEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/warehouses/products", entity, String.class);

        assertThat(resultEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultEntity.getBody()).isNotNull();
        assertThat(resultEntity.getBody()).contains("There were 2 imported products");
    }

    @Test
    @Order(11)
    void sellProduct_ProductNotAvailableException() {

        ResponseEntity<ApiError> resultEntity = restTemplate.exchange("http://localhost:" + port + "/api/warehouses/product/" + UUID.randomUUID().toString() + "/quantity/" + 1, HttpMethod.PUT, new HttpEntity<>(new HttpHeaders()), ApiError.class);

        assertThat(resultEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resultEntity.getBody()).isNotNull();
        assertThat(resultEntity.getBody().getMessage()).isEqualTo("Product is not found");
    }

    @Test
    @Order(12)
    void sellProduct_ProductNotAvailableInRequiredQuantity() {
        List<Product> productList = productRepository.findAll();
        if (productList.isEmpty()) {
            insertDataSQL();
            productList = productRepository.findAll();
        }
        UUID productId = productList.get(0).getId();

        ResponseEntity<ApiError> resultEntity = restTemplate.exchange("http://localhost:" + port + "/api/warehouses/product/" + productId.toString() + "/quantity/" + 10, HttpMethod.PUT, new HttpEntity<>(new HttpHeaders()), ApiError.class);

        assertThat(resultEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resultEntity.getBody()).isNotNull();
        assertThat(resultEntity.getBody().getMessage()).isEqualTo("Product 'test1' is not available in stock with quantity '10'");
    }

    @Test
    @Order(13)
    void sellProduct_Success() {
        List<Product> productList = productRepository.findAll();
        if (productList.isEmpty()) {
            insertDataSQL();
            productList = productRepository.findAll();
        }
        UUID productId = productList.get(0).getId();

        ResponseEntity<com.ikea.assignment.warehouse.api.model.Product> resultEntity = restTemplate.exchange("http://localhost:" + port + "/api/warehouses/product/" + productId.toString() + "/quantity/" + 1, HttpMethod.PUT, new HttpEntity<>(new HttpHeaders()), com.ikea.assignment.warehouse.api.model.Product.class);

        assertThat(resultEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultEntity.getBody()).isNotNull();
        assertThat(resultEntity.getBody().getId()).isEqualTo(productId);

    }

    @Test
    @Order(14)
    void deleteProduct_ProductNotFoundException() {

        ResponseEntity<ApiError> resultEntity = restTemplate.exchange("http://localhost:" + port + "/api/warehouses/product/" + UUID.randomUUID().toString(), HttpMethod.DELETE, new HttpEntity<>(new HttpHeaders()), ApiError.class);

        assertThat(resultEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resultEntity.getBody()).isNotNull();
        assertThat(resultEntity.getBody().getMessage()).isEqualTo("Product is not found");

    }

    @Test
    @Order(15)
    void deleteProduct_Success() {
        List<Product> productList = productRepository.findAll();
        if (productList.isEmpty()) {
            insertDataSQL();
            productList = productRepository.findAll();
        }
        UUID productId = productList.get(0).getId();

        ResponseEntity<Products> resultEntity = restTemplate.exchange("http://localhost:" + port + "/api/warehouses/product/" + productId.toString(), HttpMethod.DELETE, new HttpEntity<>(new HttpHeaders()), Products.class);

        assertThat(resultEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultEntity.getBody()).isNotNull();
        assertThat(resultEntity.getBody().getListOfProducts()).isNotEmpty();

    }

    private void insertDataSQL() {
        addInventory(12L, "5", "leg");
        addInventory(17L, "6", "screw");
        addInventory(2L, "7", "seat");
        addInventory(1L, "8", "table top");

        addProduct("test1", getArticles(Map.of("5", 4L, "6", 8L, "7", 1L)));
        addProduct("test2", getArticles(Map.of("5", 4L, "6", 8L, "8", 1L)));
    }

    private void addInventory(Long stock, String articleId, String name) {
        Inventory inventory = new Inventory();
        inventory.setStock(stock);
        inventory.setArticleId(articleId);
        inventory.setName(name);
        inventoryRepository.save(inventory);
    }

    private void addProduct(String name, List<Article> articles) {
        Product product = new Product();
        product.setName(name);
        articles.replaceAll(article -> {
            article.setProduct(product);
            return article;
        });
        product.getArticles().addAll(articles);
        productRepository.save(product);
    }

    private List<Article> getArticles(Map<String, Long> articleMap) {
        return articleMap.entrySet().stream().map((entry) -> {
            Article article = new Article();
            Optional<Inventory> optionalInventory = inventoryRepository.findByArticleId(entry.getKey());
            optionalInventory.ifPresent(article::setInventory);
            article.setAmount(entry.getValue());
            return article;
        }).collect(Collectors.toList());
    }
}


