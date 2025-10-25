package com.budgetmanager.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import jakarta.transaction.Transactional;

import com.budgetmanager.entity.*;
import java.math.BigDecimal;

@QuarkusTest
public abstract class BaseResourceTest {

    protected static final String API_BASE = "/api";

    @BeforeEach
    @Transactional
    void cleanDatabase() {
        // Ordre important pour respecter les contraintes de clés étrangères
        TransfertObjectif.deleteAll();
        Transaction.deleteAll();
        ChargeFixe.deleteAll();
        SalaireValide.deleteAll();
        ObjectifRepartition.deleteAll();
        Objectif.deleteAll();
        Compte.deleteAll();
        User.deleteAll();
        Banque.deleteAll();
    }

    protected User createTestUser() {
        User user = new User("Durand", "Jean", 15,
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