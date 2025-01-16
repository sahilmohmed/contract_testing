package com.consumer_mock.consumer.clients;

import com.consumer_mock.consumer.models.Item;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

@SuppressWarnings("null")
@Service
public class ItemServiceClient {
  @Autowired
  private RestTemplate restTemplate;

  @Value("${serviceClients.inventory.baseUrl}")
  private String baseUrl;

  /**
   * Post an item to rest template
   * 
   * @param itemMap - Json object
   * @return response entity with Item type
   * @throws JsonProcessingException
   */
  public ResponseEntity<Item> postItem(ObjectNode itemMap) throws JsonProcessingException {
    ResponseEntity<Item> entity = restTemplate.exchange(baseUrl + "/api/items", HttpMethod.POST,
        new HttpEntity<>(itemMap, auth()), Item.class);
    return entity;
  }

  /**
   * Post an item to rest template
   * 
   * @param itemMap - Json object map
   * @return response entity with String type
   * @throws JsonProcessingException
   */
  public ResponseEntity<String> postItemString(ObjectNode itemMap) throws JsonProcessingException {
    ResponseEntity<String> entity = restTemplate.exchange(baseUrl + "/api/items", HttpMethod.POST,
        new HttpEntity<>(itemMap, auth()), String.class);
    return entity;
  }

  /**
   * get an item using rest template
   * 
   * @param id - String id of the item
   * @return response entity with Item type
   * @throws JsonProcessingException
   */
  public ResponseEntity<Item> getItem(String id) {
    ResponseEntity<Item> entity = restTemplate.exchange(baseUrl + "/api/items/" + id, HttpMethod.GET,
        new HttpEntity<>(auth()), Item.class);
    return entity;
  }

  /**
   * get an item using rest template
   * 
   * @param id - String id of the item
   * @return response entity with String type
   * @throws JsonProcessingException
   */
  public ResponseEntity<String> getItemString(String id) {
    ResponseEntity<String> entity = restTemplate.exchange(baseUrl + "/api/items/" + id, HttpMethod.GET,
        new HttpEntity<>(auth()), String.class);
    return entity;
  }

  /**
   * put an item using rest template
   * 
   * @param id      - String id of the item
   * @param itemMap - Json object map
   * @return response entity with String type
   * @throws JsonProcessingException
   */
  public ResponseEntity<String> putItem(String id, Object itemMap) {
    HttpHeaders headers = auth();
    headers.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<String> entity = restTemplate.exchange(baseUrl + "/api/items/" + id, HttpMethod.PUT,
        new HttpEntity<>(itemMap.toString(), headers), String.class);
    return entity;

  }

  /**
   * delete an item using rest template
   * 
   * @param id - String id of the item
   * @return response entity with String type
   * @throws JsonProcessingException
   */
  public ResponseEntity<String> deleteItem(String id) {
    ResponseEntity<String> entity = restTemplate.exchange(baseUrl + "/api/items/" + id, HttpMethod.DELETE,
        new HttpEntity<>(auth()), String.class);
    return entity;
  }

  /**
   * Get the base URL
   * 
   * @return String base URL
   */
  public String getBaseUrl() {
    return baseUrl;
  }

  /**
   * set base URL
   * 
   * @param baseUrl String base URL
   */
  public void setBaseUrl(String baseUrl) {
    restTemplate.setErrorHandler(new CustomResponseErrorHandler());
    this.baseUrl = baseUrl;
  }

  /**
   * return a header with content type as json
   * 
   * @return header
   */
  public Map<String, String> getJsonHeader() {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");
    return headers;
  }

  /**
   * return a head with content type as text/plain
   * 
   * @return header
   */
  public Map<String, String> getTextHeader() {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "text/plain;charset=UTF-8");
    return headers;
  }

  /**
   * Sets a bearer authorization header value with an encoded Base64 Long (to
   * String)
   * 
   * @return header
   */
  private HttpHeaders auth() {
    HttpHeaders headers = new HttpHeaders();
    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    buffer.putLong(System.currentTimeMillis());
    headers.setBearerAuth(Base64.getEncoder().encodeToString(buffer.array()));
    return headers;
  }

}
