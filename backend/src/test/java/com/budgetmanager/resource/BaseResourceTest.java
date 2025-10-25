package com.budgetmanager.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import com.budgetmanager.entity.*;
import java.math.BigDecimal;

@QuarkusTest
public abstract class BaseResourceTest {

    protected static final String API_BASE = "/api";

    @Inject
    EntityManager em;

    @BeforeEach
    @Transactional
    void cleanAndSetupDatabase() {
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

        // Create a default user that will be available for all tests
        User defaultUser = new User("Durand", "Jean", 15,
                new BigDecimal("2500.00"), new BigDecimal("1000.00"));
        defaultUser.persist();

        // Flush and clear to ensure data is committed
        em.flush();
        em.clear();
    }

    protected User createTestUser() {
        User user = User.find("nom = ?1 and prenom = ?2", "Durand", "Jean").firstResult();
        if (user != null) {
            return user;
        }
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