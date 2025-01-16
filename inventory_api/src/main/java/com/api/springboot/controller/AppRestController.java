package com.api.springboot.controller;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.api.springboot.model.Inventory;
import com.api.springboot.repositories.InventoryRepository;

@RestController
@RequestMapping("/api")
public class AppRestController {

    @Autowired
    private InventoryRepository repository;

    /**
     * Adding a new item to the inventory
     * 
     * @param input
     * @return 201 Created status with the new item as the response
     */
    @RequestMapping(method = RequestMethod.POST, value = "/items")
    public ResponseEntity<?> addItem(@RequestBody Inventory input) {
        if (!isValid(input)) {
            return new ResponseEntity<>("Invalid input data", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(repository.save(input), HttpStatus.CREATED);
    }

    /**
     * Get item from the inventory
     * 
     * @param id
     * @return Item details
     */
    @RequestMapping(method = RequestMethod.GET, value = "/items/{id}")
    public ResponseEntity<?> getItem(@PathVariable("id") String id) {
        if (!repository.findById(id).isPresent()) {
            return new ResponseEntity<>("Item does not exist", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(repository.findById(id), HttpStatus.OK);
    }

    /**
     * Edit an item in the inventory
     * 
     * @param id
     * @param input
     * @return Items new details
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/items/{id}")
    public ResponseEntity<?> editItem(@PathVariable("id") String id, @RequestBody Inventory input) {
        if (!isValid(input)) {
            return new ResponseEntity<>("Invalid input data", HttpStatus.BAD_REQUEST);
        }

        if (!repository.findById(id).isPresent()) {
            return new ResponseEntity<>("Item does not exist", HttpStatus.NOT_FOUND);
        }
        Optional<Inventory> item = repository.findById(id);

        item.get().setName(input.getName());
        item.get().setQuantity(input.getQuantity());
        item.get().setPrice(input.getPrice());
        repository.saveAll(repository.findAll());
        return new ResponseEntity<>(repository.findById(id), HttpStatus.OK);
    }

    /**
     * Delete an item from the inventory
     * 
     * @param id
     * @return Http status confirming success
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/items/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable("id") String id) {
        if (!repository.findById(id).isPresent()) {
            return new ResponseEntity<>("Item does not exist", HttpStatus.NOT_FOUND);
        } else {
            repository.deleteById(id);
            return new ResponseEntity<Object>("Successful deletion", HttpStatus.NO_CONTENT);
        }
    }

    /**
     * Ensures the variables in the inventory object are not null or blank or do not
     * contain special chars
     * 
     * @param input
     * @return
     */
    private Boolean isValid(Inventory input) {
        boolean check = false;
        if (input.getName() != null) {
            Pattern specialChars = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
            Matcher matcher = specialChars.matcher(input.getName());
            check = matcher.find();
        }
        if (check || input.getName() == null || input.getName().isBlank() || input.getQuantity() <= 0
                || input.getPrice() <= 0) {
            return false;
        }
        return true;
    }

}