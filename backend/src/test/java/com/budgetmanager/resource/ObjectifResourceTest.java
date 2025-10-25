package com.budgetmanager.resource;

import com.budgetmanager.dto.CreateObjectifRequest;
import com.budgetmanager.dto.CreateRepartitionRequest;
import com.budgetmanager.dto.UpdateObjectifRequest;
import com.budgetmanager.entity.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour ObjectifResource.
 * Teste les endpoints REST incluant la création avec répartitions initiales.
 */
@QuarkusTest
class ObjectifResourceTest extends BaseResourceTest {

    @Test
    @TestTransaction
    void testGetAllObjectifs_WhenEmpty() {
        User user = createTestUser();

        given()
            .when().get(API_BASE + "/objectifs")
            .then()
            .statusCode(200)
            .body("$", hasSize(0));
    }

    @Test
    @TestTransaction
    void testGetAllObjectifs_WithMultipleObjectifs() {
        User user = createTestUser();

        Objectif obj1 = new Objectif(user, "Epargne Sécurité",
            new BigDecimal("6000.00"), PrioriteObjectif.CRITIQUE, TypeObjectif.SECURITE);
        obj1.setCouleur("#FF0000");
        obj1.persist();

        Objectif obj2 = new Objectif(user, "Vacances",
            new BigDecimal("2000.00"), PrioriteObjectif.NORMALE, TypeObjectif.PLAISIR);
        obj2.setCouleur("#00FF00");
        obj2.persist();

        given()
            .when().get(API_BASE + "/objectifs")
            .then()
            .statusCode(200)
            .body("$", hasSize(2))
            .body("[0].nom", is("Epargne Sécurité"))
            .body("[1].nom", is("Vacances"));
    }

    @Test
    @TestTransaction
    void testGetObjectif_ById() {
        User user = createTestUser();
        Objectif objectif = new Objectif(user, "Test Objectif",
            new BigDecimal("5000.00"), PrioriteObjectif.HAUTE, TypeObjectif.COURT_TERME);
        objectif.setCouleur("#4CAF50");
        objectif.persist();

        given()
            .when().get(API_BASE + "/objectifs/" + objectif.getId())
            .then()
            .statusCode(200)
            .body("id", is(objectif.getId().toString()))
            .body("nom", is("Test Objectif"))
            .body("montantCible", is(5000.00f))
            .body("priorite", is("HAUTE"))
            .body("type", is("COURT_TERME"))
            .body("couleur", is("#4CAF50"));
    }

    @Test
    @TestTransaction
    void testGetObjectif_NotFound() {
        createTestUser();

        given()
            .when().get(API_BASE + "/objectifs/00000000-0000-0000-0000-000000000000")
            .then()
            .statusCode(404);
    }

    @Test
    @TestTransaction
    void testCreateObjectif_WithoutRepartitions() {
        User user = createTestUser();

        CreateObjectifRequest request = new CreateObjectifRequest();
        request.setNom("Nouvel Objectif");
        request.setMontantCible(new BigDecimal("8000.00"));
        request.setPriorite(PrioriteObjectif.NORMALE);
        request.setType(TypeObjectif.MOYEN_TERME);
        request.setCouleur("#3F51B5");
        request.setIcone("🎯");
        request.setDescription("Test description");

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post(API_BASE + "/objectifs")
            .then()
            .statusCode(201)
            .body("nom", is("Nouvel Objectif"))
            .body("montantCible", is(8000.00f))
            .body("couleur", is("#3F51B5"))
            .body("icone", is("🎯"))
            .body("montantActuel", is(0.0f))
            .body("pourcentageProgression", is(0.00f));
    }

    @Test
    @TestTransaction
    void testCreateObjectif_WithInitialRepartitions() {
        User user = createTestUser();
        Banque banque = createTestBanque();

        Compte compte1 = new Compte(user, banque, "Livret A",
            TypeCompte.LIVRET_A, new BigDecimal("5000.00"));
        compte1.persist();

        Compte compte2 = new Compte(user, banque, "PEL",
            TypeCompte.PEL, new BigDecimal("3000.00"));
        compte2.persist();

        CreateObjectifRequest request = new CreateObjectifRequest();
        request.setNom("Epargne avec Repartitions");
        request.setMontantCible(new BigDecimal("10000.00"));
        request.setPriorite(PrioriteObjectif.HAUTE);
        request.setType(TypeObjectif.SECURITE);
        request.setCouleur("#4CAF50");

        // Ajouter les répartitions initiales
        List<CreateObjectifRequest.InitialRepartition> repartitions = new ArrayList<>();

        CreateObjectifRequest.InitialRepartition rep1 = new CreateObjectifRequest.InitialRepartition();
        rep1.setAccountId(compte1.getId().toString());
        rep1.setMontant(new BigDecimal("2490.00"));
        repartitions.add(rep1);

        CreateObjectifRequest.InitialRepartition rep2 = new CreateObjectifRequest.InitialRepartition();
        rep2.setAccountId(compte2.getId().toString());
        rep2.setMontant(new BigDecimal("1500.00"));
        repartitions.add(rep2);

        request.setRepartitions(repartitions);

        String objectifId = given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post(API_BASE + "/objectifs")
            .then()
            .statusCode(201)
            .body("nom", is("Epargne avec Repartitions"))
            .body("montantActuel", is(3990.00f))  // 2490 + 1500
            .body("pourcentageProgression", is(39.90f))  // 3990/10000 * 100
            .body("repartitions", hasSize(2))
            .extract().path("id");

        // Vérifier que les répartitions ont bien été créées
        long repartitionCount = ObjectifRepartition.count("objectif.id", objectifId);
        assertEquals(2, repartitionCount);
    }

    @Test
    @TestTransaction
    void testCreateObjectif_WithInvalidAccountId() {
        User user = createTestUser();

        CreateObjectifRequest request = new CreateObjectifRequest();
        request.setNom("Test Invalid Account");
        request.setMontantCible(new BigDecimal("5000.00"));
        request.setPriorite(PrioriteObjectif.NORMALE);
        request.setType(TypeObjectif.DIVERS);
        request.setCouleur("#FF5722");

        List<CreateObjectifRequest.InitialRepartition> repartitions = new ArrayList<>();
        CreateObjectifRequest.InitialRepartition rep = new CreateObjectifRequest.InitialRepartition();
        rep.setAccountId("00000000-0000-0000-0000-000000000000");  // Compte inexistant
        rep.setMontant(new BigDecimal("1000.00"));
        repartitions.add(rep);

        request.setRepartitions(repartitions);

        // L'objectif devrait être créé mais la répartition ignorée
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post(API_BASE + "/objectifs")
            .then()
            .statusCode(201)
            .body("nom", is("Test Invalid Account"))
            .body("montantActuel", is(0.0f))  // Aucune répartition créée
            .body("repartitions", hasSize(0));
    }

    @Test
    @TestTransaction
    void testUpdateObjectif() {
        User user = createTestUser();
        Objectif objectif = new Objectif(user, "Original Name",
            new BigDecimal("5000.00"), PrioriteObjectif.NORMALE, TypeObjectif.COURT_TERME);
        objectif.setCouleur("#000000");
        objectif.persist();

        UpdateObjectifRequest request = new UpdateObjectifRequest();
        request.setNom("Updated Name");
        request.setMontantCible(new BigDecimal("7000.00"));
        request.setPriorite(PrioriteObjectif.HAUTE);
        request.setCouleur("#FF0000");

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().put(API_BASE + "/objectifs/" + objectif.getId())
            .then()
            .statusCode(200)
            .body("nom", is("Updated Name"))
            .body("montantCible", is(7000.00f))
            .body("priorite", is("HAUTE"))
            .body("couleur", is("#FF0000"));
    }

    @Test
    @TestTransaction
    void testDeleteObjectif() {
        User user = createTestUser();
        Objectif objectif = new Objectif(user, "To Delete",
            new BigDecimal("5000.00"), PrioriteObjectif.BASSE, TypeObjectif.DIVERS);
        objectif.persist();

        given()
            .when().delete(API_BASE + "/objectifs/" + objectif.getId())
            .then()
            .statusCode(200)
            .body("message", containsString("succès"));

        // Vérifier que l'objectif est soft deleted
        Objectif deleted = Objectif.findById(objectif.getId());
        assertNotNull(deleted);
        assertFalse(deleted.getActif());
    }

    @Test
    @TestTransaction
    void testGetRepartitions() {
        User user = createTestUser();
        Banque banque = createTestBanque();

        Compte compte = new Compte(user, banque, "Test Compte",
            TypeCompte.LIVRET_A, new BigDecimal("5000.00"));
        compte.persist();

        Objectif objectif = new Objectif(user, "Test",
            new BigDecimal("10000.00"), PrioriteObjectif.NORMALE, TypeObjectif.SECURITE);
        objectif.persist();

        ObjectifRepartition rep = new ObjectifRepartition(objectif, compte,
            new BigDecimal("2500.00"));
        rep.setOrdre(1);
        rep.persist();

        given()
            .when().get(API_BASE + "/objectifs/" + objectif.getId() + "/repartitions")
            .then()
            .statusCode(200)
            .body("$", hasSize(1))
            .body("[0].montantActuel", is(2500.00f))
            .body("[0].ordre", is(1));
    }

    @Test
    @TestTransaction
    void testAjouterRepartition() {
        User user = createTestUser();
        Banque banque = createTestBanque();

        Compte compte = new Compte(user, banque, "Test Compte",
            TypeCompte.PEL, new BigDecimal("5000.00"));
        compte.persist();

        Objectif objectif = new Objectif(user, "Test",
            new BigDecimal("10000.00"), PrioriteObjectif.NORMALE, TypeObjectif.MOYEN_TERME);
        objectif.persist();

        CreateRepartitionRequest request = new CreateRepartitionRequest();
        request.setCompteId(compte.getId());
        request.setMontantActuel(new BigDecimal("1500.00"));
        request.setPourcentageCible(new BigDecimal("50.00"));
        request.setOrdre(1);

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post(API_BASE + "/objectifs/" + objectif.getId() + "/repartitions")
            .then()
            .statusCode(201)
            .body("montantActuel", is(1500.00f))
            .body("ordre", is(1))
            .body("pourcentageCible", is(50.00f));
    }

    @Test
    @TestTransaction
    void testAjouterRepartition_DuplicateCompte() {
        User user = createTestUser();
        Banque banque = createTestBanque();

        Compte compte = new Compte(user, banque, "Test Compte",
            TypeCompte.LIVRET_A, new BigDecimal("5000.00"));
        compte.persist();

        Objectif objectif = new Objectif(user, "Test",
            new BigDecimal("10000.00"), PrioriteObjectif.NORMALE, TypeObjectif.SECURITE);
        objectif.persist();

        ObjectifRepartition existing = new ObjectifRepartition(objectif, compte,
            new BigDecimal("1000.00"));
        existing.setOrdre(1);
        existing.persist();

        CreateRepartitionRequest request = new CreateRepartitionRequest();
        request.setCompteId(compte.getId());
        request.setMontantActuel(new BigDecimal("500.00"));
        request.setOrdre(2);

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post(API_BASE + "/objectifs/" + objectif.getId() + "/repartitions")
            .then()
            .statusCode(400)
            .body("message", containsString("existe déjà"));
    }

    @Test
    @TestTransaction
    void testDeleteRepartition() {
        User user = createTestUser();
        Banque banque = createTestBanque();

        Compte compte = new Compte(user, banque, "Test Compte",
            TypeCompte.LIVRET_A, new BigDecimal("5000.00"));
        compte.persist();

        Objectif objectif = new Objectif(user, "Test",
            new BigDecimal("10000.00"), PrioriteObjectif.NORMALE, TypeObjectif.SECURITE);
        objectif.persist();

        ObjectifRepartition rep = new ObjectifRepartition(objectif, compte,
            new BigDecimal("2500.00"));
        rep.setOrdre(1);
        rep.persist();

        given()
            .when().delete(API_BASE + "/objectifs/" + objectif.getId() + "/repartitions/" + rep.getId())
            .then()
            .statusCode(200)
            .body("message", containsString("succès"));

        // Vérifier que la répartition est supprimée
        assertNull(ObjectifRepartition.findById(rep.getId()));
    }
}
