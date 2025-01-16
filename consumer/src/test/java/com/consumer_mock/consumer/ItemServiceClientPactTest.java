package com.consumer_mock.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.PactSpecVersion;

import com.consumer_mock.consumer.clients.ItemServiceClient;
import com.consumer_mock.consumer.models.Item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.io.IOException;
import java.util.UUID;

@SuppressWarnings("null")
@SpringBootTest
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "Inventory")
class ItemServiceClientPactTest {

    private static final String VALID_ID = "43900953-da25-4d04-b3c2-ecd8958fac1c";
    private static final String VALID_NAME = "Iphone 13";
    private static final int VALID_QUANTITY = 12;
    private static final float VALID_PRICE = (float) 4999.99;
    private static final String JSON_STRING = "application/json";
    private static final String TEXT_STRING = "text/plain;charset=UTF-8";
    private static final String INVALID_NAME = "!!!!";
    private static final String INVALID_ID = "123";
    private static final String INVALID_QUANTITY = "abc";
    private static final String INVALID_PRICE = "abc";

    @Autowired
    private ItemServiceClient itemService;
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode itemMap = mapper.createObjectNode();

    @Pact(consumer = "ItemsCatalogue")
    public RequestResponsePact singleItem(PactDslWithProvider builder) {
        return builder
                .given("item with valid ID and body exists", "id",
                        VALID_ID, "name", VALID_NAME, "quantity", VALID_QUANTITY, "price", VALID_PRICE)
                .uponReceiving("get item with valid ID")
                .path("/api/items/" + VALID_ID)
                .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
                .willRespondWith()
                .status(200)
                .headers(itemService.getJsonHeader())
                .body(
                        new PactDslJsonBody()
                                .stringType("id", VALID_ID)
                                .stringType("name", VALID_NAME)
                                .integerType("quantity", VALID_QUANTITY)
                                .numberType("price", VALID_PRICE))
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "singleItem", pactVersion = PactSpecVersion.V3)
    void testSingleItem(MockServer mockServer) {
        itemService.setBaseUrl(mockServer.getUrl());
        Item item = itemService.getItem(VALID_ID).getBody();
        assertThat(item,
                is(equalTo(new Item(VALID_ID, VALID_NAME, VALID_QUANTITY, VALID_PRICE))));
        assertThat(
                itemService.getItem(VALID_ID).getHeaders().get("Content-Type")
                        .contains(JSON_STRING),
                is(true));
        assertThat(itemService.getItem(VALID_ID).getStatusCode().value(),
                is(equalTo(200)));
    }

    @Pact(consumer = "ItemsCatalogue")
    public RequestResponsePact missingItem(PactDslWithProvider builder) {
        return builder
                .given("item with invalid ID does not exist")
                .uponReceiving("get item with invalid ID")
                .path("/api/items/" + INVALID_ID)
                .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
                .willRespondWith()
                .status(404)
                .headers(itemService.getTextHeader())
                .body("Item does not exist")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "missingItem", pactVersion = PactSpecVersion.V3)
    void testMissingItem(MockServer mockServer) {
        itemService.setBaseUrl(mockServer.getUrl());
        String itemBody = itemService.getItemString(INVALID_ID).getBody();
        assertThat(itemBody, is(equalTo("Item does not exist")));
        assertThat(itemService.getItemString(INVALID_ID).getHeaders().get("Content-Type").contains(TEXT_STRING),
                is(true));
        assertThat(itemService.getItemString(INVALID_ID).getStatusCode().value(), is(equalTo(404)));
    }

    @Pact(consumer = "ItemsCatalogue")
    public RequestResponsePact addItem(PactDslWithProvider builder) {
        itemMap.put("name", VALID_NAME);
        itemMap.put("quantity", VALID_QUANTITY);
        itemMap.put("price", VALID_PRICE);

        return builder
                .given("item with valid ID does not exist", "id", VALID_ID)
                .uponReceiving("add valid item")
                .method("POST")
                .headers(itemService.getJsonHeader())
                .body(itemMap.toString())
                .path("/api/items")
                .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
                .willRespondWith()
                .status(201)
                .headers(itemService.getJsonHeader())
                .body(
                        new PactDslJsonBody()
                                .stringType("id", VALID_ID)
                                .stringType("name", itemMap.get("name").asText())
                                .integerType("quantity",
                                        Integer.parseInt(itemMap.get("quantity").toString()))
                                .numberType("price", Float.parseFloat(itemMap.get("price").toString())))
                .toPact();

    }

    @Test
    @PactTestFor(pactMethod = "addItem", pactVersion = PactSpecVersion.V3)
    void testAddItem(MockServer mockServer) throws JsonProcessingException {

        itemService.setBaseUrl(mockServer.getUrl());
        Item item = itemService.postItem(itemMap).getBody();
        assertThat(item,
                is(equalTo(new Item(VALID_ID, itemMap.get("name").asText(),
                        Integer.parseInt(itemMap.get("quantity").toString()),
                        Float.parseFloat(itemMap.get("price").toString())))));
        assertThat(itemService.postItem(itemMap).getHeaders().get("Content-Type").contains(JSON_STRING),
                is(true));
        assertThat(itemService.postItem(itemMap).getStatusCode().value(), is(equalTo(201)));
        assertThat(UUID.fromString(item.getId()).toString(),
                is(equalTo(item.getId())));

    }

    @Pact(consumer = "ItemsCatalogue")
    public RequestResponsePact addEmptyBodyItem(PactDslWithProvider builder) {
        ObjectNode emptyMap = new ObjectMapper().createObjectNode();
        return builder
                .given("adding an item with an empty request body should be rejected")
                .uponReceiving("add item with empty body")
                .method("POST")
                .headers(itemService.getJsonHeader())
                .body(emptyMap.toString())
                .path("/api/items")
                .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
                .willRespondWith()
                .status(400)
                .headers(itemService.getTextHeader())
                .body("Invalid input data")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "addEmptyBodyItem", pactVersion = PactSpecVersion.V3)
    void testEmptyBodyItem(MockServer mockServer) throws JsonProcessingException {
        ObjectNode emptyMap = new ObjectMapper().createObjectNode();
        itemService.setBaseUrl(mockServer.getUrl());
        assertThat(itemService.postItemString(emptyMap).getBody(), is(equalTo("Invalid input data")));
        assertThat(itemService.postItemString(emptyMap).getHeaders().get("Content-Type").contains(TEXT_STRING),
                is(true));
        assertThat(itemService.postItemString(emptyMap).getStatusCode().value(), is(equalTo(400)));
    }

    @Pact(consumer = "ItemsCatalogue")
    public RequestResponsePact addInvalidItemName(PactDslWithProvider builder) {

        itemMap.put("name", INVALID_NAME);
        itemMap.put("quantity", VALID_QUANTITY);
        itemMap.put("price", VALID_PRICE);

        return builder
                .given("adding an item with invalid name should be rejected")
                .uponReceiving("add item with invalid name")
                .method("POST")
                .headers(itemService.getJsonHeader())
                .body(itemMap.toString())
                .path("/api/items")
                .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
                .willRespondWith()
                .status(400)
                .headers(itemService.getTextHeader())
                .body("Invalid input data")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "addInvalidItemName", pactVersion = PactSpecVersion.V3)
    void testAddInvalidItemName(MockServer mockServer) throws JsonProcessingException {
        itemService.setBaseUrl(mockServer.getUrl());
        assertThat(itemService.postItemString(itemMap).getBody(), is(equalTo("Invalid input data")));
        assertThat(itemService.postItemString(itemMap).getHeaders().get("Content-Type").contains(TEXT_STRING),
                is(true));
        assertThat(itemService.postItemString(itemMap).getStatusCode().value(), is(equalTo(400)));
    }

    @Pact(consumer = "ItemsCatalogue")
    public RequestResponsePact addInvalidItemQuantity(PactDslWithProvider builder) {

        itemMap.put("name", VALID_NAME);
        itemMap.put("quantity", INVALID_QUANTITY);
        itemMap.put("price", VALID_PRICE);

        return builder
                .given("adding an item with invalid quantity should be rejected")
                .uponReceiving("add item with invalid quantity")
                .method("POST")
                .headers(itemService.getJsonHeader())
                .body(itemMap.toString())
                .path("/api/items")
                .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
                .willRespondWith()
                .status(400)
                .headers(itemService.getTextHeader())
                .body("Invalid input data")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "addInvalidItemQuantity", pactVersion = PactSpecVersion.V3)
    void testAddInvalidItemQuantity(MockServer mockServer) throws JsonProcessingException {
        itemService.setBaseUrl(mockServer.getUrl());
        assertThat(itemService.postItemString(itemMap).getBody(), is(equalTo("Invalid input data")));
        assertThat(itemService.postItemString(itemMap).getHeaders().get("Content-Type").contains(TEXT_STRING),
                is(true));
        assertThat(itemService.postItemString(itemMap).getStatusCode().value(), is(equalTo(400)));
    }

    @Pact(consumer = "ItemsCatalogue")
    public RequestResponsePact addInvalidItemPrice(PactDslWithProvider builder) {

        itemMap.put("name", VALID_NAME);
        itemMap.put("quantity", VALID_QUANTITY);
        itemMap.put("price", INVALID_PRICE);

        return builder
                .given("adding a item with an invalid price should be rejected")
                .uponReceiving("add item with invalid price")
                .method("POST")
                .headers(itemService.getJsonHeader())
                .body(itemMap.toString())
                .path("/api/items")
                .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
                .willRespondWith()
                .status(400)
                .headers(itemService.getTextHeader())
                .body("Invalid input data")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "addInvalidItemPrice", pactVersion = PactSpecVersion.V3)
    void testAddInvalidItemPrice(MockServer mockServer) throws JsonProcessingException {
        itemService.setBaseUrl(mockServer.getUrl());
        assertThat(itemService.postItemString(itemMap).getBody(), is(equalTo("Invalid input data")));
        assertThat(itemService.postItemString(itemMap).getHeaders().get("Content-Type").contains(TEXT_STRING),
                is(true));
        assertThat(itemService.postItemString(itemMap).getStatusCode().value(), is(equalTo(400)));
    }

    @Pact(consumer = "ItemsCatalogue")
    public RequestResponsePact addInvalidItemKeyName(PactDslWithProvider builder) {

        itemMap.put("abc", VALID_NAME);
        itemMap.put("quantity", VALID_QUANTITY);
        itemMap.put("price", VALID_PRICE);

        return builder
                .given("adding a item with an invalid name should be rejected")
                .uponReceiving("add item with invalid name key")
                .method("POST")
                .headers(itemService.getJsonHeader())
                .body(itemMap.toString())
                .path("/api/items")
                .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
                .willRespondWith()
                .status(400)
                .headers(itemService.getTextHeader())
                .body("Invalid input data")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "addInvalidItemKeyName", pactVersion = PactSpecVersion.V3)
    void testAddInvalidItemKeyName(MockServer mockServer) throws JsonProcessingException {
        itemService.setBaseUrl(mockServer.getUrl());
        assertThat(itemService.postItemString(itemMap).getBody(), is(equalTo("Invalid input data")));
        assertThat(itemService.postItemString(itemMap).getHeaders().get("Content-Type").contains(TEXT_STRING),
                is(true));
        assertThat(itemService.postItemString(itemMap).getStatusCode().value(), is(equalTo(400)));
    }

    @Pact(consumer = "ItemsCatalogue")
    public RequestResponsePact addInvalidItemKeyQuantity(PactDslWithProvider builder) {

        itemMap.put("name", VALID_NAME);
        itemMap.put("abc", VALID_QUANTITY);
        itemMap.put("price", VALID_PRICE);

        return builder
                .given("adding a item with an invalid quantity should be rejected")
                .uponReceiving("add item with invalid quantity key")
                .method("POST")
                .headers(itemService.getJsonHeader())
                .body(itemMap.toString())
                .path("/api/items")
                .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
                .willRespondWith()
                .status(400)
                .headers(itemService.getTextHeader())
                .body("Invalid input data")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "addInvalidItemKeyQuantity", pactVersion = PactSpecVersion.V3)
    void testAddInvalidItemKeyQuantity(MockServer mockServer) throws JsonProcessingException {
        itemService.setBaseUrl(mockServer.getUrl());
        assertThat(itemService.postItemString(itemMap).getBody(), is(equalTo("Invalid input data")));
        assertThat(itemService.postItemString(itemMap).getHeaders().get("Content-Type").contains(TEXT_STRING),
                is(true));
        assertThat(itemService.postItemString(itemMap).getStatusCode().value(), is(equalTo(400)));
    }

    @Pact(consumer = "ItemsCatalogue")
    public RequestResponsePact addInvalidItemKeyPrice(PactDslWithProvider builder) {

        itemMap.put("name", VALID_NAME);
        itemMap.put("quantity", VALID_QUANTITY);
        itemMap.put("abc", VALID_PRICE);

        return builder
                .given("adding a item with an invalid price should be rejected")
                .uponReceiving("add item with invalid price key")
                .method("POST")
                .headers(itemService.getJsonHeader())
                .body(itemMap.toString())
                .path("/api/items")
                .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
                .willRespondWith()
                .status(400)
                .headers(itemService.getTextHeader())
                .body("Invalid input data")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "addInvalidItemKeyPrice", pactVersion = PactSpecVersion.V3)
    void testAddInvalidItemKeyPrice(MockServer mockServer) throws JsonProcessingException {
        itemService.setBaseUrl(mockServer.getUrl());
        assertThat(itemService.postItemString(itemMap).getBody(), is(equalTo("Invalid input data")));
        assertThat(itemService.postItemString(itemMap).getHeaders().get("Content-Type").contains(TEXT_STRING),
                is(true));
        assertThat(itemService.postItemString(itemMap).getStatusCode().value(), is(equalTo(400)));
    }

    @Pact(consumer = "ItemsCatalogue")
    public RequestResponsePact editItem(PactDslWithProvider builder) {

        itemMap.put("name", VALID_NAME);
        itemMap.put("quantity", VALID_QUANTITY);
        itemMap.put("price", VALID_PRICE);

        return builder
                .given("item with valid ID and body exists", "id", VALID_ID, "name", VALID_NAME, "quantity",
                        VALID_QUANTITY,
                        "price", VALID_PRICE)
                .uponReceiving("edit item with valid data")
                .method("PUT")
                .headers(itemService.getJsonHeader())
                .body(itemMap.toString())
                .path("/api/items/" + VALID_ID)
                .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
                .willRespondWith()
                .status(200)
                .headers(itemService.getJsonHeader())
                .body(
                        new PactDslJsonBody()
                                .stringType("id", VALID_ID)
                                .stringType("name", itemMap.get("name").asText())
                                .integerType("quantity",
                                        Integer.parseInt(itemMap.get("quantity").toString()))
                                .numberType("price", Float.parseFloat(itemMap.get("price").toString())))
                .toPact();

    }

    @Test
    @PactTestFor(pactMethod = "editItem", pactVersion = PactSpecVersion.V3)
    void testEditItem(MockServer mockServer) throws IOException {
        itemService.setBaseUrl(mockServer.getUrl());
        String item = itemService.putItem(VALID_ID, itemMap).getBody();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actual = mapper.readTree(item);
        Item actualItem = new Item(actual.get("id").asText(),
                actual.get("name").asText(),
                Integer.parseInt(actual.get("quantity").toString()),
                Float.parseFloat(actual.get("price").toString()));

        assertThat(actualItem,
                is(equalTo(new Item(VALID_ID, itemMap.get("name").asText(),
                        Integer.parseInt(itemMap.get("quantity").toString()),
                        Float.parseFloat(itemMap.get("price").toString())))));
        assertThat(itemService.putItem(VALID_ID, itemMap).getHeaders().get("Content-Type")
                .contains(JSON_STRING), is(true));
        assertThat(itemService.putItem(VALID_ID, itemMap).getStatusCode().value(),
                is(equalTo(200)));
        assertThat(UUID.fromString(actualItem.getId()).toString(),
                is(equalTo(actualItem.getId())));

    }

    @Pact(consumer = "ItemsCatalogue")
    public RequestResponsePact editMissingItem(PactDslWithProvider builder) {

        itemMap.put("name", VALID_NAME);
        itemMap.put("quantity", VALID_QUANTITY);
        itemMap.put("price", VALID_PRICE);

        return builder
                .given("editing an item with invalid ID should be rejected")
                .uponReceiving("edit item with invalid ID")
                .method("PUT")
                .body(itemMap.toString())
                .path("/api/items/" + INVALID_ID)
                .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
                .willRespondWith()
                .status(404)
                .headers(itemService.getTextHeader())
                .body("Item does not exist")
                .toPact();

    }

    @Test
    @PactTestFor(pactMethod = "editMissingItem", pactVersion = PactSpecVersion.V3)
    void testEditMissingItem(MockServer mockServer) throws JsonProcessingException {

        itemService.setBaseUrl(mockServer.getUrl());
        assertThat(itemService.putItem(INVALID_ID, itemMap).getBody(), is(equalTo("Item does not exist")));
        assertThat(itemService.putItem(INVALID_ID,
                itemMap).getHeaders().get("Content-Type").contains(TEXT_STRING),
                is(true));
        assertThat(itemService.putItem(INVALID_ID, itemMap).getStatusCode().value(),
                is(equalTo(404)));
    }

    @Pact(consumer = "ItemsCatalogue")
    public RequestResponsePact editItemEmptyBody(PactDslWithProvider builder) {
        ObjectNode emptyMap = new ObjectMapper().createObjectNode();

        return builder
                .given("item with valid ID exists", "id", VALID_ID)
                .uponReceiving("editing an item with an empty body should be rejected")
                .method("PUT")
                .headers(itemService.getJsonHeader())
                .body(emptyMap.toString())
                .path("/api/items/" + VALID_ID)
                .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
                .willRespondWith()
                .status(400)
                .headers(itemService.getTextHeader())
                .body("Invalid input data")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "editItemEmptyBody", pactVersion = PactSpecVersion.V3)
    void testEditItemEmptyBody(MockServer mockServer) throws JsonProcessingException {
        ObjectNode emptyMap = new ObjectMapper().createObjectNode();

        itemService.setBaseUrl(mockServer.getUrl());
        assertThat(itemService.putItem(VALID_ID, emptyMap).getBody(),
                is(equalTo("Invalid input data")));
        assertThat(itemService.putItem(VALID_ID, emptyMap).getHeaders().get("Content-Type")
                .contains(TEXT_STRING), is(true));
        assertThat(itemService.putItem(VALID_ID, emptyMap).getStatusCode().value(),
                is(equalTo(400)));
    }

    @Pact(consumer = "ItemsCatalogue")
    public RequestResponsePact editInvalidItemName(PactDslWithProvider builder) {

        itemMap.put("name", INVALID_NAME);
        itemMap.put("quantity", VALID_QUANTITY);
        itemMap.put("price", VALID_PRICE);

        return builder
                .given("item with valid ID exists", "id", VALID_ID)
                .uponReceiving("edit item with invalid name")
                .method("PUT")
                .headers(itemService.getJsonHeader())
                .body(itemMap.toString())
                .path("/api/items/" + VALID_ID)
                .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
                .willRespondWith()
                .status(400)
                .headers(itemService.getTextHeader())
                .body("Invalid input data")
                .toPact();

    }

    @Test
    @PactTestFor(pactMethod = "editInvalidItemName", pactVersion = PactSpecVersion.V3)
    void testEditInvalidItemName(MockServer mockServer) throws JsonProcessingException {

        itemService.setBaseUrl(mockServer.getUrl());
        assertThat(itemService.putItem(VALID_ID, itemMap).getBody(),
                is(equalTo("Invalid input data")));
        assertThat(itemService.putItem(VALID_ID, itemMap).getHeaders().get("Content-Type")
                .contains(TEXT_STRING), is(true));
        assertThat(itemService.putItem(VALID_ID, itemMap).getStatusCode().value(),
                is(equalTo(400)));

    }

    @Pact(consumer = "ItemsCatalogue")
    public RequestResponsePact editInvalidItemQuantity(PactDslWithProvider builder) {

        itemMap.put("name", VALID_NAME);
        itemMap.put("quantity", INVALID_QUANTITY);
        itemMap.put("price", VALID_PRICE);

        return builder
                .given("item with valid ID exists", "id", VALID_ID)
                .uponReceiving("edit item with invalid quantity")
                .method("PUT")
                .headers(itemService.getJsonHeader())
                .body(itemMap.toString())
                .path("/api/items/" + VALID_ID)
                .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
                .willRespondWith()
                .status(400)
                .headers(itemService.getTextHeader())
                .body("Invalid input data")
                .toPact();

    }

    @Test
    @PactTestFor(pactMethod = "editInvalidItemQuantity", pactVersion = PactSpecVersion.V3)
    void testEditInvalidItemQuantity(MockServer mockServer) throws JsonProcessingException {

        itemService.setBaseUrl(mockServer.getUrl());
        assertThat(itemService.putItem(VALID_ID, itemMap).getBody(),
                is(equalTo("Invalid input data")));
        assertThat(itemService.putItem(VALID_ID, itemMap).getHeaders().get("Content-Type")
                .contains(TEXT_STRING), is(true));
        assertThat(itemService.putItem(VALID_ID, itemMap).getStatusCode().value(),
                is(equalTo(400)));

    }

    @Pact(consumer = "ItemsCatalogue")
    public RequestResponsePact editInvalidItemPrice(PactDslWithProvider builder) {

        itemMap.put("name", VALID_NAME);
        itemMap.put("quantity", VALID_QUANTITY);
        itemMap.put("price", INVALID_PRICE);

        return builder
                .given("item with valid ID exists", "id", VALID_ID)
                .uponReceiving("edit item with invalid price")
                .method("PUT")
                .headers(itemService.getJsonHeader())
                .body(itemMap.toString())
                .path("/api/items/" + VALID_ID)
                .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
                .willRespondWith()
                .status(400)
                .headers(itemService.getTextHeader())
                .body("Invalid input data")
                .toPact();

    }

    @Test
    @PactTestFor(pactMethod = "editInvalidItemPrice", pactVersion = PactSpecVersion.V3)
    void testEditInvalidItemPrice(MockServer mockServer) throws JsonProcessingException {

        itemService.setBaseUrl(mockServer.getUrl());
        assertThat(itemService.putItem(VALID_ID, itemMap).getBody(),
                is(equalTo("Invalid input data")));
        assertThat(itemService.putItem(VALID_ID, itemMap).getHeaders().get("Content-Type")
                .contains(TEXT_STRING), is(true));
        assertThat(itemService.putItem(VALID_ID, itemMap).getStatusCode().value(),
                is(equalTo(400)));
    }

    @Pact(consumer = "ItemsCatalogue")
    public RequestResponsePact editInvalidItemNameKey(PactDslWithProvider builder) {

        itemMap.put("abc", VALID_NAME);
        itemMap.put("quantity", VALID_QUANTITY);
        itemMap.put("price", VALID_PRICE);

        return builder
                .given("item with valid ID exists", "id", VALID_ID)
                .uponReceiving("edit item with invalid name key")
                .method("PUT")
                .headers(itemService.getJsonHeader())
                .body(itemMap.toString())
                .path("/api/items/" + VALID_ID)
                .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
                .willRespondWith()
                .status(400)
                .headers(itemService.getTextHeader())
                .body("Invalid input data")
                .toPact();

    }

    @Test
    @PactTestFor(pactMethod = "editInvalidItemNameKey", pactVersion = PactSpecVersion.V3)
    void testEditInvalidItemNameKey(MockServer mockServer) throws JsonProcessingException {

        itemService.setBaseUrl(mockServer.getUrl());
        assertThat(itemService.putItem(VALID_ID, itemMap).getBody(),
                is(equalTo("Invalid input data")));
        assertThat(itemService.putItem(VALID_ID, itemMap).getHeaders().get("Content-Type")
                .contains(TEXT_STRING), is(true));
        assertThat(itemService.putItem(VALID_ID, itemMap).getStatusCode().value(),
                is(equalTo(400)));
    }

    @Pact(consumer = "ItemsCatalogue")
    public RequestResponsePact editInvalidItemQuantityKey(PactDslWithProvider builder) {

        itemMap.put("name", VALID_NAME);
        itemMap.put("abc", VALID_QUANTITY);
        itemMap.put("price", VALID_PRICE);

        return builder
                .given("item with valid ID exists", "id", VALID_ID)
                .uponReceiving("edit item with invalid quantity key")
                .method("PUT")
                .headers(itemService.getJsonHeader())
                .body(itemMap.toString())
                .path("/api/items/" + VALID_ID)
                .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
                .willRespondWith()
                .status(400)
                .headers(itemService.getTextHeader())
                .body("Invalid input data")
                .toPact();

    }

    @Test
    @PactTestFor(pactMethod = "editInvalidItemQuantityKey", pactVersion = PactSpecVersion.V3)
    void testEditInvalidItemQuantityKey(MockServer mockServer) throws JsonProcessingException {

        itemService.setBaseUrl(mockServer.getUrl());
        assertThat(itemService.putItem(VALID_ID, itemMap).getBody(),
                is(equalTo("Invalid input data")));
        assertThat(itemService.putItem(VALID_ID, itemMap).getHeaders().get("Content-Type")
                .contains(TEXT_STRING), is(true));
        assertThat(itemService.putItem(VALID_ID, itemMap).getStatusCode().value(),
                is(equalTo(400)));
    }

    @Pact(consumer = "ItemsCatalogue")
    public RequestResponsePact editInvalidItemPriceKey(PactDslWithProvider builder) {

        itemMap.put("name", VALID_NAME);
        itemMap.put("quantity", VALID_QUANTITY);
        itemMap.put("abc", VALID_PRICE);

        return builder
                .given("item with valid ID exists", "id", VALID_ID)
                .uponReceiving("edit item with invalid price key")
                .method("PUT")
                .headers(itemService.getJsonHeader())
                .body(itemMap.toString())
                .path("/api/items/" + VALID_ID)
                .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
                .willRespondWith()
                .status(400)
                .headers(itemService.getTextHeader())
                .body("Invalid input data")
                .toPact();

    }

    @Test
    @PactTestFor(pactMethod = "editInvalidItemPriceKey", pactVersion = PactSpecVersion.V3)
    void testEditInvalidItemPriceKey(MockServer mockServer) throws JsonProcessingException {

        itemService.setBaseUrl(mockServer.getUrl());
        assertThat(itemService.putItem(VALID_ID, itemMap).getBody(),
                is(equalTo("Invalid input data")));
        assertThat(itemService.putItem(VALID_ID, itemMap).getHeaders().get("Content-Type")
                .contains(TEXT_STRING), is(true));
        assertThat(itemService.putItem(VALID_ID, itemMap).getStatusCode().value(),
                is(equalTo(400)));
    }

    @Pact(consumer = "ItemsCatalogue")
    public RequestResponsePact deleteItem(PactDslWithProvider builder) {
        return builder
                .given("item with valid ID exists", "id", VALID_ID)
                .uponReceiving("delete item with valid ID")
                .method("DELETE")
                .path("/api/items/" + VALID_ID)
                .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
                .willRespondWith()
                .status(204)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "deleteItem", pactVersion = PactSpecVersion.V3)
    void testDeleteItem(MockServer mockServer) throws JsonProcessingException {
        itemService.setBaseUrl(mockServer.getUrl());
        assertThat(itemService.deleteItem(VALID_ID).getHeaders().get("Content-Type") == null, is(true));
        assertThat(itemService.deleteItem(VALID_ID).getStatusCode().value(), is(equalTo(204)));
    }

    @Pact(consumer = "ItemsCatalogue")
    public RequestResponsePact deleteInvalidItem(PactDslWithProvider builder) {
        return builder
                .given("delete an item with invalid ID")
                .uponReceiving("delete item with invalid ID")
                .method("DELETE")
                .path("/api/items/" + INVALID_ID)
                .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
                .willRespondWith()
                .status(404)
                .headers(itemService.getTextHeader())
                .body("Item does not exist")
                .toPact();

    }

    @Test
    @PactTestFor(pactMethod = "deleteInvalidItem", pactVersion = PactSpecVersion.V3)
    void testDeleteInvalidItem(MockServer mockServer) throws JsonProcessingException {
        itemService.setBaseUrl(mockServer.getUrl());
        assertThat(itemService.deleteItem(INVALID_ID).getBody(), is(equalTo("Item does not exist")));
        assertThat(itemService.deleteItem(INVALID_ID).getHeaders().get("Content-Type").contains(TEXT_STRING),
                is(true));
        assertThat(itemService.deleteItem(INVALID_ID).getStatusCode().value(), is(equalTo(404)));
    }






  @Pact(consumer = "ItemsCatalogue")
  public RequestResponsePact noAuthTokenGetItem(PactDslWithProvider builder) {
    return builder
    .given("item with valid ID exists", "id", VALID_ID)
    .uponReceiving("get a item with no auth token")
        .path("/api/items/" + VALID_ID)
      .willRespondWith()
        .status(401)
      .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "noAuthTokenGetItem", pactVersion = PactSpecVersion.V3)
  void testNoAuthTokenGetItem(MockServer mockServer) {
    itemService.setBaseUrl(mockServer.getUrl());
    assertThat(itemService.getItem(VALID_ID).getStatusCode().toString(), containsString("401 UNAUTHORIZED"));
    assertThat(itemService.getItem(VALID_ID).getStatusCode().value(), is(equalTo(401)));
  }

  @Pact(consumer = "ItemsCatalogue")
  public RequestResponsePact noAuthTokenPostItem(PactDslWithProvider builder) {
    itemMap.put("name", VALID_NAME);
    itemMap.put("quantity", VALID_QUANTITY);
    itemMap.put("price", VALID_PRICE);

    return builder
    .given("item with valid ID does not exist", "id", VALID_ID)
    .uponReceiving("add a item with no auth token")
    .method("POST")
    .headers(itemService.getJsonHeader())
    .body(itemMap.toString())
        .path("/api/items")
      .willRespondWith()
        .status(401)
      .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "noAuthTokenPostItem", pactVersion = PactSpecVersion.V3)
  void testNoAuthTokenPostItem(MockServer mockServer) throws JsonProcessingException {
    itemService.setBaseUrl(mockServer.getUrl());
    assertThat(itemService.postItem(itemMap).getStatusCode().toString(), containsString("401 UNAUTHORIZED"));
    assertThat(itemService.postItem(itemMap).getStatusCode().value(), is(equalTo(401)));
  }


  @Pact(consumer = "ItemsCatalogue")
  public RequestResponsePact noAuthTokenPutItem(PactDslWithProvider builder) {
    itemMap.put("name", VALID_NAME);
    itemMap.put("quantity", VALID_QUANTITY);
    itemMap.put("price", VALID_PRICE);

    return builder
    .given("item with valid ID exists", "id", VALID_ID)
    .uponReceiving("edit a item with no auth token")
    .method("PUT")
    .headers(itemService.getJsonHeader())
    .body(itemMap.toString())
        .path("/api/items/"+VALID_ID)
      .willRespondWith()
        .status(401)
      .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "noAuthTokenPutItem", pactVersion = PactSpecVersion.V3)
  void testNoAuthTokenPutItem(MockServer mockServer) throws JsonProcessingException {
    itemService.setBaseUrl(mockServer.getUrl());
    assertThat(itemService.putItem(VALID_ID,itemMap).getStatusCode().toString(), containsString("401 UNAUTHORIZED"));
    assertThat(itemService.putItem(VALID_ID,itemMap).getStatusCode().value(), is(equalTo(401)));
  }

  @Pact(consumer = "ItemsCatalogue")
  public RequestResponsePact noAuthTokenDeleteItem(PactDslWithProvider builder) {
    return builder
    .given("item with valid ID exists", "id", VALID_ID)
    .uponReceiving("delete a item with no auth token")
    .method("DELETE")
        .path("/api/items/"+VALID_ID)
      .willRespondWith()
        .status(401)
      .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "noAuthTokenDeleteItem", pactVersion = PactSpecVersion.V3)
  void testNoAuthTokenDeleteItem(MockServer mockServer) throws JsonProcessingException {
    itemService.setBaseUrl(mockServer.getUrl());
    assertThat(itemService.deleteItem(VALID_ID).getStatusCode().toString(), containsString("401 UNAUTHORIZED"));
    assertThat(itemService.deleteItem(VALID_ID).getStatusCode().value(), is(equalTo(401)));
  }




}
