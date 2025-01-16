package com.consumer_mock.consumer.models;

import lombok.Data;
/**
 * Lombok DTO for the item in the Inventory tool
 */

@Data
public class Item {
  private final String id;
  private final String name;
  private final int quantity;
  private final float price;
}
