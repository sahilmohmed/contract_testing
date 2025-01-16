package com.api.springboot;

import java.nio.ByteBuffer;
import java.util.Base64;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.api.springboot.repositories.InventoryRepository;

@SpringBootApplication
public class InventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
        System.out.println("=================== Bearer token for testing ============================");
        System.out.println(generateToken());
        System.out.println("=========================================================================");

    }

    @Bean
    public CommandLineRunner initializeDb(InventoryRepository repository) {
        return (args) -> {
        };

    }
    private static String generateToken() {
    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    buffer.putLong(System.currentTimeMillis());
    return Base64.getEncoder().encodeToString(buffer.array());
  }
}
