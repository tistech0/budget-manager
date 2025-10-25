package com.budgetmanager.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeAll;
import io.quarkus.test.junit.QuarkusTestProfile;

import com.budgetmanager.entity.*;
import java.math.BigDecimal;
import java.util.Map;

@QuarkusTest
public abstract class BaseResourceTest {

    protected static final String API_BASE = "/api";

    // No @BeforeEach to avoid database connection issues
    // Tests should use @TestTransaction on individual test methods

    protected User createTestUser() {
        // Try to find existing user first
        User user = User.find("nom = ?1 and prenom = ?2", "Durand", "Jean").firstResult();
        if (user != null) {
            return user;
        }
        // Create new user if not found
        user = new User("Durand", "Jean", 15,
                new BigDecimal("2500.00"), new BigDecimal("1000.00"));
        user.persist();
        return user;
    }

    protected Banque createTestBanque() {
        Banque banque = new Banque("Fortuneo Test", "#FF6600", "/logos/test.png");
        banque.persist();
        return banque;
    }
}