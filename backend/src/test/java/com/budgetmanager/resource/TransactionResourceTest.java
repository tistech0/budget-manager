package com.budgetmanager.resource;

import com.budgetmanager.dto.*;
import com.budgetmanager.entity.*;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for TransactionResource
 *
 * Critical endpoints tested:
 * - GET /api/transactions (with filtering)
 * - GET /api/transactions/{id}
 * - POST /api/transactions (create)
 * - PUT /api/transactions/{id} (update with reversal)
 * - DELETE /api/transactions/{id} (delete with reversal)
 * - POST /api/transactions/salaire (salary validation with automatic charges fixes)
 *
 * Business logic verified:
 * - Account balance updates
 * - ObjectifRepartition updates
 * - Reversal logic on updates/deletes
 * - Salary validation flow
 * - Automatic charges fixes processing
 * - Transaction filtering and pagination
 */
@QuarkusTest
class TransactionResourceTest {

    @Inject
    EntityManager entityManager;

    private User testUser;
    private Banque testBanque;
    private Compte testCompteCourant;
    private Compte testLivretA;
    private Objectif testObjectif;
    private ObjectifRepartition testRepartition;
    private ChargeFixe testChargeFixe;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean database in correct order (respecting foreign keys)
        TransfertObjectif.deleteAll();
        Transaction.deleteAll();
        ChargeFixe.deleteAll();
        SalaireValide.deleteAll();
        ObjectifRepartition.deleteAll();
        Objectif.deleteAll();
        Compte.deleteAll();
        User.deleteAll();
        Banque.deleteAll();

        // Create test user
        testUser = new User("Test", "User", 15,
                new BigDecimal("2500.00"), new BigDecimal("500.00"));
        testUser.persist();

        // Create test bank
        testBanque = new Banque("Test Bank", "#FF0000", null);
        testBanque.persist();

        // Create test accounts
        testCompteCourant = new Compte(testUser, testBanque, "Compte Courant Test",
                TypeCompte.COMPTE_COURANT, new BigDecimal("1000.00"));
        testCompteCourant.setPrincipalChargesFixes(true);
        testCompteCourant.persist();

        testLivretA = new Compte(testUser, testBanque, "Livret A Test",
                TypeCompte.LIVRET_A, new BigDecimal("5000.00"));
        testLivretA.persist();

        // Create test objectif
        testObjectif = new Objectif(testUser, "Épargne Test",
                new BigDecimal("10000.00"), PrioriteObjectif.HAUTE, TypeObjectif.SECURITE);
        testObjectif.persist();

        // Create test repartition
        testRepartition = new ObjectifRepartition(testObjectif, testCompteCourant,
                new BigDecimal("500.00"));
        testRepartition.setOrdre(1);
        testRepartition.persist();

        // Create test charge fixe
        testChargeFixe = new ChargeFixe(testUser, testCompteCourant,
                "Loyer Test", "Loyer mensuel",
                new BigDecimal("800.00"), TypeTransaction.LOYER,
                5, FrequenceCharge.MENSUELLE, LocalDate.now().withDayOfMonth(1));
        testChargeFixe.persist();
    }

    // ========== GET /api/transactions TESTS ==========

    @Test
    void getAllTransactions_ShouldReturnAllTransactions() {
        // Given: Create some transactions
        createTestTransaction(testCompteCourant, new BigDecimal("100.00"),
                TypeTransaction.SALAIRE, "Salaire Test");
        createTestTransaction(testCompteCourant, new BigDecimal("-50.00"),
                TypeTransaction.ALIMENTATION, "Courses");
        createTestTransaction(testLivretA, new BigDecimal("200.00"),
                TypeTransaction.EPARGNE, "Épargne");

        // When: Get all transactions
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/transactions")
        .then()
            .statusCode(200)
            .body("$", hasSize(3))
            .body("[0].description", notNullValue())
            .body("[0].montant", notNullValue())
            .body("[0].type", notNullValue());
    }

    @Test
    void getAllTransactions_ShouldFilterByDateRange() {
        // Given: Transactions on different dates
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastWeek = today.minusDays(7);

        createTestTransactionWithDate(testCompteCourant, new BigDecimal("100.00"),
                TypeTransaction.SALAIRE, "Today", today);
        createTestTransactionWithDate(testCompteCourant, new BigDecimal("-50.00"),
                TypeTransaction.ALIMENTATION, "Yesterday", yesterday);
        createTestTransactionWithDate(testLivretA, new BigDecimal("200.00"),
                TypeTransaction.EPARGNE, "Last Week", lastWeek);

        // When: Filter by date range (yesterday to today)
        given()
            .contentType(ContentType.JSON)
            .queryParam("dateDebut", yesterday.toString())
            .queryParam("dateFin", today.toString())
        .when()
            .get("/api/transactions")
        .then()
            .statusCode(200)
            .body("$", hasSize(2))
            .body("[0].dateTransaction", anyOf(is(today.toString()), is(yesterday.toString())));
    }

    @Test
    void getAllTransactions_ShouldFilterByType() {
        // Given: Transactions with different types
        createTestTransaction(testCompteCourant, new BigDecimal("100.00"),
                TypeTransaction.SALAIRE, "Salaire");
        createTestTransaction(testCompteCourant, new BigDecimal("-50.00"),
                TypeTransaction.ALIMENTATION, "Courses");
        createTestTransaction(testCompteCourant, new BigDecimal("-30.00"),
                TypeTransaction.ALIMENTATION, "Restaurant");

        // When: Filter by ALIMENTATION type
        given()
            .contentType(ContentType.JSON)
            .queryParam("type", "ALIMENTATION")
        .when()
            .get("/api/transactions")
        .then()
            .statusCode(200)
            .body("$", hasSize(2))
            .body("[0].type", is("ALIMENTATION"))
            .body("[1].type", is("ALIMENTATION"));
    }

    @Test
    void getAllTransactions_ShouldFilterByCompte() {
        // Given: Transactions on different accounts
        createTestTransaction(testCompteCourant, new BigDecimal("100.00"),
                TypeTransaction.SALAIRE, "CC Transaction");
        createTestTransaction(testLivretA, new BigDecimal("200.00"),
                TypeTransaction.EPARGNE, "Livret Transaction");

        // When: Filter by compte courant
        given()
            .contentType(ContentType.JSON)
            .queryParam("compteId", testCompteCourant.getId().toString())
        .when()
            .get("/api/transactions")
        .then()
            .statusCode(200)
            .body("$", hasSize(1))
            .body("[0].compte.id", is(testCompteCourant.getId().toString()));
    }

    @Test
    void getAllTransactions_ShouldRespectLimit() {
        // Given: Create many transactions
        for (int i = 0; i < 150; i++) {
            createTestTransaction(testCompteCourant, new BigDecimal("10.00"),
                    TypeTransaction.SALAIRE, "Transaction " + i);
        }

        // When: Get with default limit (100)
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/transactions")
        .then()
            .statusCode(200)
            .body("$", hasSize(100));

        // When: Get with custom limit (50)
        given()
            .contentType(ContentType.JSON)
            .queryParam("limit", 50)
        .when()
            .get("/api/transactions")
        .then()
            .statusCode(200)
            .body("$", hasSize(50));
    }

    // ========== GET /api/transactions/{id} TESTS ==========

    @Test
    void getTransaction_ShouldReturnTransaction_WhenExists() {
        // Given: A transaction
        Transaction transaction = createTestTransaction(testCompteCourant,
                new BigDecimal("100.00"), TypeTransaction.SALAIRE, "Test Transaction");

        // When: Get by ID
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/transactions/" + transaction.getId())
        .then()
            .statusCode(200)
            .body("id", is(transaction.getId().toString()))
            .body("description", is("Test Transaction"))
            .body("montant", is(100.00f))
            .body("type", is("SALAIRE"))
            .body("compte.id", is(testCompteCourant.getId().toString()));
    }

    @Test
    void getTransaction_ShouldReturn404_WhenNotExists() {
        // When: Get non-existent transaction
        UUID randomId = UUID.randomUUID();

        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/transactions/" + randomId)
        .then()
            .statusCode(404)
            .body("message", is("Transaction non trouvée"));
    }

    // ========== POST /api/transactions TESTS ==========

    @Test
    void createTransaction_ShouldCreateTransaction_AndUpdateCompteBalance() {
        // Given: Initial balance
        BigDecimal initialBalance = testCompteCourant.getSoldeTotal();

        // When: Create transaction
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setCompteId(testCompteCourant.getId());
        request.setMontant(new BigDecimal("500.00"));
        request.setDescription("New Transaction");
        request.setType(TypeTransaction.SALAIRE);
        request.setDateTransaction(LocalDate.now().toString());

        String transactionId = given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/transactions")
        .then()
            .statusCode(201)
            .body("description", is("New Transaction"))
            .body("montant", is(500.00f))
            .body("type", is("SALAIRE"))
            .extract().path("id");

        // Then: Verify account balance updated
        entityManager.clear();
        Compte updatedCompte = Compte.findById(testCompteCourant.getId());
        assertEquals(
            initialBalance.add(new BigDecimal("500.00")),
            updatedCompte.getSoldeTotal(),
            "Account balance should be updated"
        );

        // Verify transaction persisted
        Transaction created = Transaction.findById(UUID.fromString(transactionId));
        assertNotNull(created);
        assertEquals("New Transaction", created.getDescription());
    }

    @Test
    void createTransaction_ShouldUpdateObjectifRepartition_WhenLinkedToObjectif() {
        // Given: Initial repartition amount
        BigDecimal initialAmount = testRepartition.getMontantActuel();

        // When: Create transaction linked to objectif
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setCompteId(testCompteCourant.getId());
        request.setObjectifId(testObjectif.getId());
        request.setMontant(new BigDecimal("300.00"));
        request.setDescription("Objectif Transaction");
        request.setType(TypeTransaction.VERSEMENT_OBJECTIF);

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/transactions")
        .then()
            .statusCode(201);

        // Then: Verify repartition updated
        entityManager.clear();
        ObjectifRepartition updatedRep = ObjectifRepartition.findById(testRepartition.getId());
        assertEquals(
            initialAmount.add(new BigDecimal("300.00")),
            updatedRep.getMontantActuel(),
            "Repartition should be credited"
        );
    }

    @Test
    void createTransaction_ShouldReturn404_WhenCompteNotFound() {
        // When: Create transaction with non-existent compte
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setCompteId(UUID.randomUUID());
        request.setMontant(new BigDecimal("100.00"));
        request.setDescription("Test");
        request.setType(TypeTransaction.SALAIRE);

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/transactions")
        .then()
            .statusCode(404)
            .body("message", is("Compte non trouvé"));
    }

    // ========== PUT /api/transactions/{id} TESTS ==========

    @Test
    void updateTransaction_ShouldUpdateDescription() {
        // Given: Existing transaction
        Transaction transaction = createTestTransaction(testCompteCourant,
                new BigDecimal("100.00"), TypeTransaction.SALAIRE, "Old Description");

        // When: Update description
        UpdateTransactionRequest request = new UpdateTransactionRequest();
        request.setDescription("Updated Description");

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/api/transactions/" + transaction.getId())
        .then()
            .statusCode(200)
            .body("description", is("Updated Description"));

        // Then: Verify persisted
        entityManager.clear();
        Transaction updated = Transaction.findById(transaction.getId());
        assertEquals("Updated Description", updated.getDescription());
    }

    @Test
    void updateTransaction_ShouldAdjustCompteBalance_WhenMontantChanged() {
        // Given: Transaction with 100 montant
        Transaction transaction = createTestTransaction(testCompteCourant,
                new BigDecimal("100.00"), TypeTransaction.SALAIRE, "Test");

        BigDecimal balanceAfterCreate = testCompteCourant.getSoldeTotal();

        // When: Update montant to 150
        UpdateTransactionRequest request = new UpdateTransactionRequest();
        request.setMontant(new BigDecimal("150.00"));

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/api/transactions/" + transaction.getId())
        .then()
            .statusCode(200)
            .body("montant", is(150.00f));

        // Then: Balance should increase by 50 (difference)
        entityManager.clear();
        Compte updatedCompte = Compte.findById(testCompteCourant.getId());
        assertEquals(
            balanceAfterCreate.add(new BigDecimal("50.00")),
            updatedCompte.getSoldeTotal(),
            "Balance should be adjusted by difference"
        );
    }

    @Test
    void updateTransaction_ShouldAdjustRepartition_WhenLinkedToObjectif() {
        // Given: Transaction linked to objectif with 300 montant
        CreateTransactionRequest createReq = new CreateTransactionRequest();
        createReq.setCompteId(testCompteCourant.getId());
        createReq.setObjectifId(testObjectif.getId());
        createReq.setMontant(new BigDecimal("300.00"));
        createReq.setDescription("Objectif Transaction");
        createReq.setType(TypeTransaction.VERSEMENT_OBJECTIF);

        String transactionId = given()
            .contentType(ContentType.JSON)
            .body(createReq)
        .when()
            .post("/api/transactions")
        .then()
            .statusCode(201)
            .extract().path("id");

        entityManager.clear();
        ObjectifRepartition repAfterCreate = ObjectifRepartition.findById(testRepartition.getId());
        BigDecimal repartitionAfterCreate = repAfterCreate.getMontantActuel();

        // When: Update montant to 500 (increase by 200)
        UpdateTransactionRequest updateReq = new UpdateTransactionRequest();
        updateReq.setMontant(new BigDecimal("500.00"));

        given()
            .contentType(ContentType.JSON)
            .body(updateReq)
        .when()
            .put("/api/transactions/" + transactionId)
        .then()
            .statusCode(200);

        // Then: Repartition should increase by 200
        entityManager.clear();
        ObjectifRepartition updatedRep = ObjectifRepartition.findById(testRepartition.getId());
        assertEquals(
            repartitionAfterCreate.add(new BigDecimal("200.00")),
            updatedRep.getMontantActuel(),
            "Repartition should be adjusted by difference"
        );
    }

    @Test
    void updateTransaction_ShouldReturn404_WhenNotExists() {
        // When: Update non-existent transaction
        UpdateTransactionRequest request = new UpdateTransactionRequest();
        request.setDescription("Test");

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/api/transactions/" + UUID.randomUUID())
        .then()
            .statusCode(404)
            .body("message", is("Transaction non trouvée"));
    }

    // ========== DELETE /api/transactions/{id} TESTS ==========

    @Test
    void deleteTransaction_ShouldReverseCompteBalance() {
        // Given: Transaction that credited account with 200
        Transaction transaction = createTestTransaction(testCompteCourant,
                new BigDecimal("200.00"), TypeTransaction.SALAIRE, "Test");

        BigDecimal balanceAfterCreate = testCompteCourant.getSoldeTotal();

        // When: Delete transaction
        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/api/transactions/" + transaction.getId())
        .then()
            .statusCode(200)
            .body("message", is("Transaction supprimée avec succès"));

        // Then: Balance should be reversed (decreased by 200)
        entityManager.clear();
        Compte updatedCompte = Compte.findById(testCompteCourant.getId());
        assertEquals(
            balanceAfterCreate.subtract(new BigDecimal("200.00")),
            updatedCompte.getSoldeTotal(),
            "Balance should be reversed"
        );

        // Verify transaction deleted
        assertNull(Transaction.findById(transaction.getId()));
    }

    @Test
    void deleteTransaction_ShouldReverseRepartition_WhenLinkedToObjectif() {
        // Given: Transaction linked to objectif
        CreateTransactionRequest createReq = new CreateTransactionRequest();
        createReq.setCompteId(testCompteCourant.getId());
        createReq.setObjectifId(testObjectif.getId());
        createReq.setMontant(new BigDecimal("400.00"));
        createReq.setDescription("Objectif Transaction");
        createReq.setType(TypeTransaction.VERSEMENT_OBJECTIF);

        String transactionId = given()
            .contentType(ContentType.JSON)
            .body(createReq)
        .when()
            .post("/api/transactions")
        .then()
            .statusCode(201)
            .extract().path("id");

        entityManager.clear();
        ObjectifRepartition repAfterCreate2 = ObjectifRepartition.findById(testRepartition.getId());
        BigDecimal repartitionAfterCreate = repAfterCreate2.getMontantActuel();

        // When: Delete transaction
        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/api/transactions/" + transactionId)
        .then()
            .statusCode(200);

        // Then: Repartition should be reversed (decreased by 400)
        entityManager.clear();
        ObjectifRepartition updatedRep = ObjectifRepartition.findById(testRepartition.getId());
        assertEquals(
            repartitionAfterCreate.subtract(new BigDecimal("400.00")),
            updatedRep.getMontantActuel(),
            "Repartition should be reversed"
        );
    }

    @Test
    void deleteTransaction_ShouldReturn404_WhenNotExists() {
        // When: Delete non-existent transaction
        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/api/transactions/" + UUID.randomUUID())
        .then()
            .statusCode(404)
            .body("message", is("Transaction non trouvée"));
    }

    // ========== POST /api/transactions/salaire TESTS ==========

    @Test
    void validerSalaire_ShouldCreateSalaireTransaction_WithDefaultAmount() {
        // Given: User has salaire mensuel net configured
        BigDecimal salaire = testUser.getSalaireMensuelNet();
        BigDecimal initialBalance = testCompteCourant.getSoldeTotal();

        // When: Validate salaire without specifying amount
        ValidationSalaireRequest request = new ValidationSalaireRequest();
        request.setMois(LocalDate.now().toString().substring(0, 7)); // YYYY-MM
        request.setType(TypeTransaction.SALAIRE);

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/transactions/salaire")
        .then()
            .statusCode(201)
            .body("montant", is(salaire.floatValue()))
            .body("type", is("SALAIRE"));

        // Then: Account balance should be credited
        entityManager.clear();
        Compte updatedCompte = Compte.findById(testCompteCourant.getId());
        assertEquals(
            initialBalance.add(salaire),
            updatedCompte.getSoldeTotal(),
            "Account should be credited with salary"
        );
    }

    @Test
    void validerSalaire_ShouldProcessChargesFixes_Automatically() {
        // Given: Charge fixe with jour prelevement = 5
        // And: Validation date is after day 5

        BigDecimal initialBalance = testCompteCourant.getSoldeTotal();

        // When: Validate salaire on day 15 (after charge fixe day 5)
        ValidationSalaireRequest request = new ValidationSalaireRequest();
        request.setMois(LocalDate.now().toString().substring(0, 7)); // YYYY-MM
        request.setType(TypeTransaction.SALAIRE);
        request.setDateReception(LocalDate.now().withDayOfMonth(15).toString());

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/transactions/salaire")
        .then()
            .statusCode(201);

        // Then: Should have created 2 transactions (salaire + charge fixe)
        entityManager.clear();
        long transactionCount = Transaction.count("user = ?1", testUser);
        assertEquals(2, transactionCount, "Should create salary + charge fixe transaction");

        // And: Charge fixe transaction should exist
        Transaction chargeTransaction = Transaction.find(
                "user = ?1 and type = ?2 and description like ?3",
                testUser, TypeTransaction.LOYER, "%Loyer Test%"
        ).firstResult();

        assertNotNull(chargeTransaction, "Charge fixe transaction should be created");
        assertEquals(
            new BigDecimal("800.00").negate(),
            chargeTransaction.getMontant(),
            "Charge fixe should be negative amount"
        );
    }

    @Test
    void validerSalaire_ShouldNotDuplicateChargesFixes_IfAlreadyProcessed() {
        // Given: Already validated salaire for this month
        String currentMonth = LocalDate.now().toString().substring(0, 7);

        ValidationSalaireRequest firstRequest = new ValidationSalaireRequest();
        firstRequest.setMois(currentMonth);
        firstRequest.setType(TypeTransaction.SALAIRE);
        firstRequest.setDateReception(LocalDate.now().withDayOfMonth(15).toString());

        given()
            .contentType(ContentType.JSON)
            .body(firstRequest)
        .when()
            .post("/api/transactions/salaire")
        .then()
            .statusCode(201);

        long transactionsAfterFirst = Transaction.count("user = ?1", testUser);

        // When: Validate salaire again for same month
        ValidationSalaireRequest secondRequest = new ValidationSalaireRequest();
        secondRequest.setMois(currentMonth);
        secondRequest.setType(TypeTransaction.PRIME); // Different type to allow creation
        secondRequest.setMontant(new BigDecimal("500.00"));
        secondRequest.setDateReception(LocalDate.now().withDayOfMonth(20).toString());

        given()
            .contentType(ContentType.JSON)
            .body(secondRequest)
        .when()
            .post("/api/transactions/salaire")
        .then()
            .statusCode(201);

        // Then: Should only create 1 new transaction (not duplicate charge fixe)
        entityManager.clear();
        long transactionsAfterSecond = Transaction.count("user = ?1", testUser);
        assertEquals(
            transactionsAfterFirst + 1,
            transactionsAfterSecond,
            "Should not duplicate charge fixe transaction"
        );
    }

    @Test
    void validerSalaire_ShouldRequireMontant_ForPrimeOrFreelance() {
        // When: Try to validate PRIME without montant
        ValidationSalaireRequest request = new ValidationSalaireRequest();
        request.setMois(LocalDate.now().toString().substring(0, 7));
        request.setType(TypeTransaction.PRIME);
        // montant is null

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/transactions/salaire")
        .then()
            .statusCode(400)
            .body("message", containsString("montant est requis"));
    }

    @Test
    void validerSalaire_ShouldReturn404_WhenNoCompteAvailable() {
        // Given: Delete all comptes
        Compte.deleteAll();
        entityManager.flush();

        // When: Try to validate salaire
        ValidationSalaireRequest request = new ValidationSalaireRequest();
        request.setMois(LocalDate.now().toString().substring(0, 7));
        request.setType(TypeTransaction.SALAIRE);

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/transactions/salaire")
        .then()
            .statusCode(404)
            .body("message", is("Aucun compte disponible"));
    }

    // ========== HELPER METHODS ==========

    private Transaction createTestTransaction(Compte compte, BigDecimal montant,
                                             TypeTransaction type, String description) {
        Transaction transaction = new Transaction();
        transaction.setUser(testUser);
        transaction.setCompte(compte);
        transaction.setMontant(montant);
        transaction.setType(type);
        transaction.setDescription(description);
        transaction.setDateTransaction(LocalDate.now());
        transaction.persist();

        // Update compte balance
        compte.setSoldeTotal(compte.getSoldeTotal().add(montant));

        entityManager.flush();
        return transaction;
    }

    private Transaction createTestTransactionWithDate(Compte compte, BigDecimal montant,
                                                     TypeTransaction type, String description,
                                                     LocalDate date) {
        Transaction transaction = new Transaction();
        transaction.setUser(testUser);
        transaction.setCompte(compte);
        transaction.setMontant(montant);
        transaction.setType(type);
        transaction.setDescription(description);
        transaction.setDateTransaction(date);
        transaction.persist();

        // Update compte balance
        compte.setSoldeTotal(compte.getSoldeTotal().add(montant));

        entityManager.flush();
        return transaction;
    }
}
