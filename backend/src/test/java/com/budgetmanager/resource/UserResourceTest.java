package com.budgetmanager.resource;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import io.restassured.http.ContentType;

import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.*;
import static io.restassured.RestAssured.given;

import com.budgetmanager.entity.User;
import com.budgetmanager.entity.Compte;
import com.budgetmanager.entity.Objectif;
import com.budgetmanager.entity.ObjectifRepartition;
import com.budgetmanager.entity.Transaction;
import com.budgetmanager.entity.ChargeFixe;
import com.budgetmanager.entity.SalaireValide;

@QuarkusTest
class UserResourceTest {

    protected static final String API_BASE = "/api";

    @Test
    void testGetProfileWhenNoUser() {
        // Nettoyer via l'API plutôt que directement
        clearUsers();

        given()
                .when().get(API_BASE + "/user/profile")
                .then()
                .statusCode(404)
                .body("message", equalTo("Aucun profil utilisateur trouvé"));
    }

    @Test
    void testCreateProfile() {
        clearUsers();

        String userJson = """
            {
                "nom": "Martin",
                "prenom": "Paul", 
                "jourPaie": 28,
                "salaireMensuelNet": 3000.00,
                "decouvertAutorise": 500.00
            }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(userJson)
                .when().post(API_BASE + "/user/profile")
                .then()
                .statusCode(201)
                .body("nom", equalTo("Martin"));
    }

    @Test
    void testGetProfile() {
        clearUsers();

        // Créer l'utilisateur via l'API au lieu de la base directement
        String userJson = """
            {
                "nom": "Durand",
                "prenom": "Jean", 
                "jourPaie": 15,
                "salaireMensuelNet": 2500.00,
                "decouvertAutorise": 1000.00
            }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(userJson)
                .when().post(API_BASE + "/user/profile")
                .then()
                .statusCode(201);

        // Puis tester le GET
        given()
                .when().get(API_BASE + "/user/profile")
                .then()
                .statusCode(200)
                .body("nom", equalTo("Durand"))
                .body("prenom", equalTo("Jean"));
    }

    @Transactional
    void clearUsers() {
        Transaction.deleteAll();
        ChargeFixe.deleteAll();
        SalaireValide.deleteAll();
        ObjectifRepartition.deleteAll();
        Objectif.deleteAll();
        Compte.deleteAll();
        User.deleteAll();
    }
}