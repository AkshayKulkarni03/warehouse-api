package com.ikea.assignment.warehouse.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/warehouses")
public class WarehouseController {

    @PostMapping(path = "/inventory", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> loadInventory(@RequestParam("file") MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        return ResponseEntity.ok("done");
    }
}
