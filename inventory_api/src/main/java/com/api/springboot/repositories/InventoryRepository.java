package com.api.springboot.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import com.api.springboot.model.Inventory;

public interface InventoryRepository extends CrudRepository<Inventory, String> {

	@SuppressWarnings("null")
	Optional<Inventory> findById(String id);

}
