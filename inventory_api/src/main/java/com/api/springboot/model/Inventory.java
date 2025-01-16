package com.api.springboot.model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Inventory")
public class Inventory {

	@Id
	// @GeneratedValue(strategy = GenerationType.UUID)
	private String id = genUUID();

	public Inventory() {

	}

	public Inventory(String id, String name, int quantity, float price) {

		this.id = id;
		this.name = name;
		this.quantity = quantity;
		this.price = price;

	}

	public Inventory(String name, int quantity, float price) {

		this.name = name;
		this.quantity = quantity;
		this.price = price;
	}

	private String name;
	private int quantity;
	private float price;

	public String genUUID() {
		return UUID.randomUUID().toString();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}
}
