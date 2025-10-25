package com.budgetmanager.resource;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeAll;
import io.quarkus.test.junit.QuarkusTestProfile;

import com.budgetmanager.entity.*;
import java.math.BigDecimal;
import java.util.Map;

@QuarkusTest
public abstract class BaseResourceTest {

    protected static final String API_BASE = "/api";

    // Helper methods now manage their own transactions and commit
    // so that data is visible to REST endpoints

    protected User createTestUser() {
        User[] userHolder = new User[1];
        QuarkusTransaction.requiringNew().run(() -> {
            // Try to find existing user first
            User user = User.find("nom = ?1 and prenom = ?2", "Durand", "Jean").firstResult();
            if (user != null) {
                userHolder[0] = user;
                return;
            }
            // Create new user if not found
            user = new User("Durand", "Jean", 15,
                    new BigDecimal("2500.00"), new BigDecimal("1000.00"));
            user.persist();
            userHolder[0] = user;
        });
        return userHolder[0];
    }

    protected Banque createTestBanque() {
        Banque[] banqueHolder = new Banque[1];
        QuarkusTransaction.requiringNew().run(() -> {
            Banque banque = new Banque("Fortuneo Test", "#FF6600", "/logos/test.png");
            banque.persist();
            banqueHolder[0] = banque;
        });
        return banqueHolder[0];
    }
}