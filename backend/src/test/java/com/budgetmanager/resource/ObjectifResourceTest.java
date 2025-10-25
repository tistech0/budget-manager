package com.budgetmanager.resource;

import com.budgetmanager.dto.CreateObjectifRequest;
import com.budgetmanager.dto.CreateRepartitionRequest;
import com.budgetmanager.dto.UpdateObjectifRequest;
import com.budgetmanager.entity.*;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    // Helper to wrap entity creation in committed transaction
    private void inTransaction(Runnable runnable) {
        QuarkusTransaction.requiringNew().run(runnable);
    }

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean database before each test
        TransfertObjectif.deleteAll();
        Transaction.deleteAll();
        ObjectifRepartition.deleteAll();
        Objectif.deleteAll();
        SalaireValide.deleteAll();
        ChargeFixe.deleteAll();
        Compte.deleteAll();
        User.deleteAll();
        Banque.deleteAll();
    }

    @Test

    void testGetAllObjectifs_WhenEmpty() {
        User user = createTestUser();

        given()
            .when().get(API_BASE + "/objectifs")
            .then()
            .statusCode(200)
            .body("$", hasSize(0));
    }

    @Test
    void testGetAllObjectifs_WithMultipleObjectifs() {
        User user = createTestUser();

        inTransaction(() -> {
            Objectif obj1 = new Objectif(user, "Epargne Sécurité",
                new BigDecimal("6000.00"), PrioriteObjectif.CRITIQUE, TypeObjectif.SECURITE);
            obj1.setCouleur("#FF0000");
            obj1.persist();

            Objectif obj2 = new Objectif(user, "Vacances",
                new BigDecimal("2000.00"), PrioriteObjectif.NORMALE, TypeObjectif.PLAISIR);
            obj2.setCouleur("#00FF00");
            obj2.persist();
        });

        given()
            .when().get(API_BASE + "/objectifs")
            .then()
            .statusCode(200)
            .body("$", hasSize(2))
            .body("[0].nom", is("Epargne Sécurité"))
            .body("[1].nom", is("Vacances"));
    }

    @Test
    void testGetObjectif_ById() {
        User user = createTestUser();

        Objectif[] objectifHolder = new Objectif[1];
        inTransaction(() -> {
            Objectif objectif = new Objectif(user, "Test Objectif",
                new BigDecimal("5000.00"), PrioriteObjectif.HAUTE, TypeObjectif.COURT_TERME);
            objectif.setCouleur("#4CAF50");
            objectif.persist();
            objectifHolder[0] = objectif;
        });
        Objectif objectif = objectifHolder[0];

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

    void testGetObjectif_NotFound() {
        createTestUser();

        given()
            .when().get(API_BASE + "/objectifs/00000000-0000-0000-0000-000000000000")
            .then()
            .statusCode(404);
    }

    @Test

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
            .body("montantActuel", is(0))
            .body("pourcentageProgression", is(0.0f));
    }

    @Test
    void testCreateObjectif_WithInitialRepartitions() {
        User user = createTestUser();
        Banque banque = createTestBanque();

        Compte[] compteHolders = new Compte[2];
        inTransaction(() -> {
            Compte compte1 = new Compte(user, banque, "Livret A",
                TypeCompte.LIVRET_A, new BigDecimal("5000.00"));
            compte1.persist();
            compteHolders[0] = compte1;

            Compte compte2 = new Compte(user, banque, "PEL",
                TypeCompte.PEL, new BigDecimal("3000.00"));
            compte2.persist();
            compteHolders[1] = compte2;
        });
        Compte compte1 = compteHolders[0];
        Compte compte2 = compteHolders[1];

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
        long repartitionCount = ObjectifRepartition.count("objectif.id", UUID.fromString(objectifId));
        assertEquals(2, repartitionCount);
    }

    @Test

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
            .body("montantActuel", is(0));  // Aucune répartition créée
    }

    @Test
    void testUpdateObjectif() {
        User user = createTestUser();

        Objectif[] objectifHolder = new Objectif[1];
        inTransaction(() -> {
            Objectif objectif = new Objectif(user, "Original Name",
                new BigDecimal("5000.00"), PrioriteObjectif.NORMALE, TypeObjectif.COURT_TERME);
            objectif.setCouleur("#000000");
            objectif.persist();
            objectifHolder[0] = objectif;
        });
        Objectif objectif = objectifHolder[0];

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
    void testDeleteObjectif() {
        User user = createTestUser();

        Objectif[] objectifHolder = new Objectif[1];
        inTransaction(() -> {
            Objectif objectif = new Objectif(user, "To Delete",
                new BigDecimal("5000.00"), PrioriteObjectif.BASSE, TypeObjectif.DIVERS);
            objectif.persist();
            objectifHolder[0] = objectif;
        });
        Objectif objectif = objectifHolder[0];

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
    void testGetRepartitions() {
        User user = createTestUser();
        Banque banque = createTestBanque();

        Objectif[] objectifHolder = new Objectif[1];
        inTransaction(() -> {
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
            objectifHolder[0] = objectif;
        });
        Objectif objectif = objectifHolder[0];

        given()
            .when().get(API_BASE + "/objectifs/" + objectif.getId() + "/repartitions")
            .then()
            .statusCode(200)
            .body("$", hasSize(1))
            .body("[0].montantActuel", is(2500.00f))
            .body("[0].ordre", is(1));
    }

    @Test
    void testAjouterRepartition() {
        User user = createTestUser();
        Banque banque = createTestBanque();

        Compte[] compteHolder = new Compte[1];
        Objectif[] objectifHolder = new Objectif[1];
        inTransaction(() -> {
            Compte compte = new Compte(user, banque, "Test Compte",
                TypeCompte.PEL, new BigDecimal("5000.00"));
            compte.persist();
            compteHolder[0] = compte;

            Objectif objectif = new Objectif(user, "Test",
                new BigDecimal("10000.00"), PrioriteObjectif.NORMALE, TypeObjectif.MOYEN_TERME);
            objectif.persist();
            objectifHolder[0] = objectif;
        });
        Compte compte = compteHolder[0];
        Objectif objectif = objectifHolder[0];

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
    void testAjouterRepartition_DuplicateCompte() {
        User user = createTestUser();
        Banque banque = createTestBanque();

        Compte[] compteHolder = new Compte[1];
        Objectif[] objectifHolder = new Objectif[1];
        inTransaction(() -> {
            Compte compte = new Compte(user, banque, "Test Compte",
                TypeCompte.LIVRET_A, new BigDecimal("5000.00"));
            compte.persist();
            compteHolder[0] = compte;

            Objectif objectif = new Objectif(user, "Test",
                new BigDecimal("10000.00"), PrioriteObjectif.NORMALE, TypeObjectif.SECURITE);
            objectif.persist();

            ObjectifRepartition existing = new ObjectifRepartition(objectif, compte,
                new BigDecimal("1000.00"));
            existing.setOrdre(1);
            existing.persist();
            objectifHolder[0] = objectif;
        });
        Compte compte = compteHolder[0];
        Objectif objectif = objectifHolder[0];

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
    void testDeleteRepartition() {
        User user = createTestUser();
        Banque banque = createTestBanque();

        Objectif[] objectifHolder = new Objectif[1];
        ObjectifRepartition[] repHolder = new ObjectifRepartition[1];
        inTransaction(() -> {
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
            objectifHolder[0] = objectif;
            repHolder[0] = rep;
        });
        Objectif objectif = objectifHolder[0];
        ObjectifRepartition rep = repHolder[0];

        given()
            .when().delete(API_BASE + "/objectifs/" + objectif.getId() + "/repartitions/" + rep.getId())
            .then()
            .statusCode(200)
            .body("message", containsString("succès"));

        // Vérifier que la répartition est supprimée
        assertNull(ObjectifRepartition.findById(rep.getId()));
    }
}
