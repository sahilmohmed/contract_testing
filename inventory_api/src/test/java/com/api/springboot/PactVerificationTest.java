package com.api.springboot;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.IgnoreMissingStateChange;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.StateChangeAction;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.api.springboot.model.Inventory;
import com.api.springboot.repositories.InventoryRepository;

import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.ProtocolException;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;
import java.util.Base64;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Provider("Inventory")
@SuppressWarnings("null")
// @PactFolder("pacts")
@PactBroker(scheme = "http", host = "localhost", port = "9292",
authentication = @PactBrokerAuth(username = "admin", password = "password"))
@IgnoreMissingStateChange
public class PactVerificationTest {
    private static final String VALID_NAME = "Iphone 13";
    private static final int VALID_QUANTITY = 12;
    private static final float VALID_PRICE = (float) 4999.99;
  @LocalServerPort
  private int port;

  @Autowired
  private InventoryRepository inventoryRepository;

  @PersistenceContext
private EntityManager em;

  @BeforeEach
  void setup(PactVerificationContext context) {
    context.setTarget(new HttpTestTarget("localhost", port));
  }

    @TestTemplate
  @ExtendWith(PactVerificationSpringProvider.class)
  void pactVerificationTestTemplate(PactVerificationContext context, HttpRequest request) throws ProtocolException {
    if (request.containsHeader("Authorization")) {
      request.setHeader("Authorization", "Bearer " + generateToken());
    }
    context.verifyInteraction();
  }

  private static String generateToken() {
    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    buffer.putLong(System.currentTimeMillis());
    return Base64.getEncoder().encodeToString(buffer.array());
  }

  @State(value = "item with valid ID and body exists", action = StateChangeAction.SETUP)
  void itemIDAndBodyExists(Map<String, Object> params) {
    Optional<Inventory> inventory = inventoryRepository.findById(params.get("id").toString());
    if (!inventory.isPresent()) {
      inventoryRepository.save(new Inventory(params.get("id").toString(), params.get("name").toString(), Integer.parseInt(params.get("quantity").toString()),Float.parseFloat(params.get("price").toString())));
    }
  }

  @State(value = "item with valid ID exists", action = StateChangeAction.SETUP)
  void itemIDExists(Map<String, Object> params) {
    Optional<Inventory> inventory = inventoryRepository.findById(params.get("id").toString());
    if (!inventory.isPresent()) {
      inventoryRepository.save(new Inventory(params.get("id").toString(), VALID_NAME, VALID_QUANTITY,VALID_PRICE));
    }
  }

  @State(value = "item with valid ID does not exist", action = StateChangeAction.SETUP)
  void itemDoesntExist(Map<String, Object> params) {
    Optional<Inventory> inventory = inventoryRepository.findById(params.get("id").toString());
    if (inventory.isPresent()) {  
      inventoryRepository.deleteById(params.get("id").toString());
    }
  }

}
