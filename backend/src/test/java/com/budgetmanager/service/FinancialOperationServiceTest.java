package com.budgetmanager.service;

import com.budgetmanager.entity.*;
import com.budgetmanager.util.MoneyConstants;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FinancialOperationService - Critical for money handling!
 *
 * Tests cover:
 * - Debit/Credit operations on Compte
 * - Debit/Credit operations on ObjectifRepartition
 * - Transfers between Comptes
 * - Transfers to/from Objectifs
 * - Edge cases (negative amounts, zero, insufficient funds)
 * - Money rounding
 * - Transaction atomicity
 */
@QuarkusTest
class FinancialOperationServiceTest {

    @Inject
    FinancialOperationService financialOps;

    @Inject
    EntityManager entityManager;

    private User testUser;
    private Banque testBanque;
    private Compte testCompte1;
    private Compte testCompte2;
    private Objectif testObjectif;
    private ObjectifRepartition testRepartition;

    @BeforeEach
    @TestTransaction
    void setUp() {
        // Clean database - respecting foreign key constraints
        TransfertObjectif.deleteAll();  // Must delete before Transaction
        Transaction.deleteAll();  // Must delete before Compte
        ChargeFixe.deleteAll();  // Must delete before Compte
        SalaireValide.deleteAll();  // Must delete before Compte
        ObjectifRepartition.deleteAll();
        Objectif.deleteAll();
        Compte.deleteAll();
        User.deleteAll();
        Banque.deleteAll();

        // Create test data
        testUser = new User("Test", "User", 15,
            new BigDecimal("2500.00"), new BigDecimal("500.00"));
        testUser.persist();

        testBanque = new Banque("Test Bank", "#FF0000", null);
        testBanque.persist();

        testCompte1 = new Compte(testUser, testBanque, "Compte Test 1",
            TypeCompte.COMPTE_COURANT, new BigDecimal("1000.00"));
        testCompte1.persist();

        testCompte2 = new Compte(testUser, testBanque, "Compte Test 2",
            TypeCompte.LIVRET_A, new BigDecimal("500.00"));
        testCompte2.persist();

        testObjectif = new Objectif(testUser, "Epargne Test",
            new BigDecimal("10000.00"), PrioriteObjectif.HAUTE, TypeObjectif.SECURITE);
        testObjectif.persist();

        testRepartition = new ObjectifRepartition(testObjectif, testCompte1,
            new BigDecimal("100.00"));
        testRepartition.setOrdre(1);
        testRepartition.persist();
    }

    // ========== DEBIT COMPTE TESTS ==========

    @Test
    @TestTransaction
    void debitCompte_ShouldReduceBalance() {
        // Given: Compte with balance 1000
        BigDecimal initialBalance = testCompte1.getSoldeTotal();
        BigDecimal debitAmount = new BigDecimal("250.00");

        // When: Debit 250
        financialOps.debitCompte(testCompte1, debitAmount);

        // Then: Balance should be 750
        assertEquals(
            initialBalance.subtract(debitAmount),
            testCompte1.getSoldeTotal()
        );
        assertEquals(new BigDecimal("750.00"), testCompte1.getSoldeTotal());
    }

    @Test
    @TestTransaction
    void debitCompte_ShouldRoundToTwoDecimals() {
        // Given: Compte with balance
        testCompte1.setSoldeTotal(new BigDecimal("1000.00"));

        // When: Debit amount with many decimals
        financialOps.debitCompte(testCompte1, new BigDecimal("33.333"));

        // Then: Result should be rounded to 2 decimals
        assertEquals(new BigDecimal("966.67"), testCompte1.getSoldeTotal());
    }

    @Test
    @TestTransaction
    void debitCompte_ShouldAllowNegativeBalance() {
        // Given: Compte with small balance
        testCompte1.setSoldeTotal(new BigDecimal("100.00"));

        // When: Debit more than balance (overdraft allowed)
        financialOps.debitCompte(testCompte1, new BigDecimal("150.00"));

        // Then: Balance should be negative
        assertEquals(new BigDecimal("-50.00"), testCompte1.getSoldeTotal());
    }

    @Test
    @TestTransaction
    void debitCompte_ShouldThrowException_WhenAmountIsZero() {
        // When/Then: Debit with zero amount should throw exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> financialOps.debitCompte(testCompte1, BigDecimal.ZERO)
        );
        assertEquals("Montant must be positive", exception.getMessage());
    }

    @Test
    @TestTransaction
    void debitCompte_ShouldThrowException_WhenAmountIsNegative() {
        // When/Then: Debit with negative amount should throw exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> financialOps.debitCompte(testCompte1, new BigDecimal("-100.00"))
        );
        assertEquals("Montant must be positive", exception.getMessage());
    }

    // ========== CREDIT COMPTE TESTS ==========

    @Test
    @TestTransaction
    void creditCompte_ShouldIncreaseBalance() {
        // Given: Compte with balance 1000
        BigDecimal initialBalance = testCompte1.getSoldeTotal();
        BigDecimal creditAmount = new BigDecimal("250.00");

        // When: Credit 250
        financialOps.creditCompte(testCompte1, creditAmount);

        // Then: Balance should be 1250
        assertEquals(
            initialBalance.add(creditAmount),
            testCompte1.getSoldeTotal()
        );
        assertEquals(new BigDecimal("1250.00"), testCompte1.getSoldeTotal());
    }

    @Test
    @TestTransaction
    void creditCompte_ShouldRoundToTwoDecimals() {
        // Given: Compte with balance
        testCompte1.setSoldeTotal(new BigDecimal("1000.00"));

        // When: Credit amount with many decimals
        financialOps.creditCompte(testCompte1, new BigDecimal("33.333"));

        // Then: Result should be rounded to 2 decimals
        assertEquals(new BigDecimal("1033.33"), testCompte1.getSoldeTotal());
    }

    @Test
    @TestTransaction
    void creditCompte_ShouldThrowException_WhenAmountIsZero() {
        // When/Then: Credit with zero amount should throw exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> financialOps.creditCompte(testCompte1, BigDecimal.ZERO)
        );
        assertEquals("Montant must be positive", exception.getMessage());
    }

    @Test
    @TestTransaction
    void creditCompte_ShouldThrowException_WhenAmountIsNegative() {
        // When/Then: Credit with negative amount should throw exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> financialOps.creditCompte(testCompte1, new BigDecimal("-100.00"))
        );
        assertEquals("Montant must be positive", exception.getMessage());
    }

    // ========== TRANSFER BETWEEN COMPTES TESTS ==========

    @Test
    @TestTransaction
    void transferBetweenComptes_ShouldTransferMoney() {
        // Given: Compte1 with 1000, Compte2 with 500
        BigDecimal initialBalance1 = testCompte1.getSoldeTotal(); // 1000
        BigDecimal initialBalance2 = testCompte2.getSoldeTotal(); // 500
        BigDecimal transferAmount = new BigDecimal("300.00");

        // When: Transfer 300 from Compte1 to Compte2
        financialOps.transferBetweenComptes(testCompte1, testCompte2, transferAmount);

        // Then: Compte1 should have 700, Compte2 should have 800
        assertEquals(
            initialBalance1.subtract(transferAmount),
            testCompte1.getSoldeTotal()
        );
        assertEquals(
            initialBalance2.add(transferAmount),
            testCompte2.getSoldeTotal()
        );
        assertEquals(new BigDecimal("700.00"), testCompte1.getSoldeTotal());
        assertEquals(new BigDecimal("800.00"), testCompte2.getSoldeTotal());
    }

    @Test
    @TestTransaction
    void transferBetweenComptes_ShouldMaintainTotalBalance() {
        // Given: Two comptes
        BigDecimal totalBefore = testCompte1.getSoldeTotal().add(testCompte2.getSoldeTotal());

        // When: Transfer money
        financialOps.transferBetweenComptes(testCompte1, testCompte2, new BigDecimal("400.00"));

        // Then: Total should remain the same (conservation of money)
        BigDecimal totalAfter = testCompte1.getSoldeTotal().add(testCompte2.getSoldeTotal());
        assertEquals(totalBefore, totalAfter);
    }

    @Test
    @TestTransaction
    void transferBetweenComptes_ShouldThrowException_WhenAmountIsZero() {
        // When/Then: Transfer with zero amount should throw exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> financialOps.transferBetweenComptes(testCompte1, testCompte2, BigDecimal.ZERO)
        );
        assertEquals("Montant must be positive", exception.getMessage());
    }

    @Test
    @TestTransaction
    void transferBetweenComptes_ShouldThrowException_WhenAmountIsNegative() {
        // When/Then: Transfer with negative amount should throw exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> financialOps.transferBetweenComptes(
                testCompte1, testCompte2, new BigDecimal("-100.00")
            )
        );
        assertEquals("Montant must be positive", exception.getMessage());
    }

    // ========== DEBIT OBJECTIF REPARTITION TESTS ==========

    @Test
    @TestTransaction
    void debitObjectifRepartition_ShouldReduceAmount() {
        // Given: Repartition with 100
        BigDecimal initialAmount = testRepartition.getMontantActuel();
        BigDecimal debitAmount = new BigDecimal("30.00");

        // When: Debit 30
        financialOps.debitObjectifRepartition(testRepartition, debitAmount);

        // Then: Amount should be 70
        assertEquals(
            initialAmount.subtract(debitAmount),
            testRepartition.getMontantActuel()
        );
        assertEquals(new BigDecimal("70.00"), testRepartition.getMontantActuel());
    }

    @Test
    @TestTransaction
    void debitObjectifRepartition_ShouldRoundToTwoDecimals() {
        // Given: Repartition with amount
        testRepartition.setMontantActuel(new BigDecimal("100.00"));

        // When: Debit amount with many decimals
        financialOps.debitObjectifRepartition(testRepartition, new BigDecimal("33.333"));

        // Then: Result should be rounded to 2 decimals
        assertEquals(new BigDecimal("66.67"), testRepartition.getMontantActuel());
    }

    @Test
    @TestTransaction
    void debitObjectifRepartition_ShouldThrowException_WhenInsufficientFunds() {
        // Given: Repartition with 100
        testRepartition.setMontantActuel(new BigDecimal("100.00"));

        // When/Then: Debit 150 should throw exception (cannot go negative)
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> financialOps.debitObjectifRepartition(testRepartition, new BigDecimal("150.00"))
        );
        assertEquals("Insufficient funds in objectif repartition", exception.getMessage());
    }

    @Test
    @TestTransaction
    void debitObjectifRepartition_ShouldAllowDebitToExactlyZero() {
        // Given: Repartition with 100
        testRepartition.setMontantActuel(new BigDecimal("100.00"));

        // When: Debit exact amount
        financialOps.debitObjectifRepartition(testRepartition, new BigDecimal("100.00"));

        // Then: Amount should be exactly zero
        assertEquals(BigDecimal.ZERO.setScale(2), testRepartition.getMontantActuel());
    }

    @Test
    @TestTransaction
    void debitObjectifRepartition_ShouldThrowException_WhenAmountIsZero() {
        // When/Then: Debit with zero amount should throw exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> financialOps.debitObjectifRepartition(testRepartition, BigDecimal.ZERO)
        );
        assertEquals("Montant must be positive", exception.getMessage());
    }

    @Test
    @TestTransaction
    void debitObjectifRepartition_ShouldThrowException_WhenAmountIsNegative() {
        // When/Then: Debit with negative amount should throw exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> financialOps.debitObjectifRepartition(
                testRepartition, new BigDecimal("-10.00")
            )
        );
        assertEquals("Montant must be positive", exception.getMessage());
    }

    // ========== CREDIT OBJECTIF REPARTITION TESTS ==========

    @Test
    @TestTransaction
    void creditObjectifRepartition_ShouldIncreaseAmount() {
        // Given: Repartition with 100
        BigDecimal initialAmount = testRepartition.getMontantActuel();
        BigDecimal creditAmount = new BigDecimal("50.00");

        // When: Credit 50
        financialOps.creditObjectifRepartition(testRepartition, creditAmount);

        // Then: Amount should be 150
        assertEquals(
            initialAmount.add(creditAmount),
            testRepartition.getMontantActuel()
        );
        assertEquals(new BigDecimal("150.00"), testRepartition.getMontantActuel());
    }

    @Test
    @TestTransaction
    void creditObjectifRepartition_ShouldRoundToTwoDecimals() {
        // Given: Repartition with amount
        testRepartition.setMontantActuel(new BigDecimal("100.00"));

        // When: Credit amount with many decimals
        financialOps.creditObjectifRepartition(testRepartition, new BigDecimal("33.333"));

        // Then: Result should be rounded to 2 decimals
        assertEquals(new BigDecimal("133.33"), testRepartition.getMontantActuel());
    }

    @Test
    @TestTransaction
    void creditObjectifRepartition_ShouldThrowException_WhenAmountIsZero() {
        // When/Then: Credit with zero amount should throw exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> financialOps.creditObjectifRepartition(testRepartition, BigDecimal.ZERO)
        );
        assertEquals("Montant must be positive", exception.getMessage());
    }

    @Test
    @TestTransaction
    void creditObjectifRepartition_ShouldThrowException_WhenAmountIsNegative() {
        // When/Then: Credit with negative amount should throw exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> financialOps.creditObjectifRepartition(
                testRepartition, new BigDecimal("-10.00")
            )
        );
        assertEquals("Montant must be positive", exception.getMessage());
    }

    // ========== TRANSFER TO OBJECTIF TESTS ==========

    @Test
    @TestTransaction
    void transferToObjectif_ShouldTransferMoneyFromCompteToRepartition() {
        // Given: Compte with 1000, Repartition with 100
        BigDecimal initialCompteBalance = testCompte1.getSoldeTotal();
        BigDecimal initialRepartitionAmount = testRepartition.getMontantActuel();
        BigDecimal transferAmount = new BigDecimal("200.00");

        // When: Transfer 200 from Compte to Repartition
        financialOps.transferToObjectif(testCompte1, testRepartition, transferAmount);

        // Then: Compte should have 800, Repartition should have 300
        assertEquals(
            initialCompteBalance.subtract(transferAmount),
            testCompte1.getSoldeTotal()
        );
        assertEquals(
            initialRepartitionAmount.add(transferAmount),
            testRepartition.getMontantActuel()
        );
        assertEquals(new BigDecimal("800.00"), testCompte1.getSoldeTotal());
        assertEquals(new BigDecimal("300.00"), testRepartition.getMontantActuel());
    }

    @Test
    @TestTransaction
    void transferToObjectif_ShouldThrowException_WhenAmountIsZero() {
        // When/Then: Transfer with zero amount should throw exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> financialOps.transferToObjectif(testCompte1, testRepartition, BigDecimal.ZERO)
        );
        assertEquals("Montant must be positive", exception.getMessage());
    }

    @Test
    @TestTransaction
    void transferToObjectif_ShouldThrowException_WhenAmountIsNegative() {
        // When/Then: Transfer with negative amount should throw exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> financialOps.transferToObjectif(
                testCompte1, testRepartition, new BigDecimal("-50.00")
            )
        );
        assertEquals("Montant must be positive", exception.getMessage());
    }

    // ========== TRANSFER FROM OBJECTIF TESTS ==========

    @Test
    @TestTransaction
    void transferFromObjectif_ShouldTransferMoneyFromRepartitionToCompte() {
        // Given: Repartition with 100, Compte with 1000
        BigDecimal initialRepartitionAmount = testRepartition.getMontantActuel();
        BigDecimal initialCompteBalance = testCompte1.getSoldeTotal();
        BigDecimal transferAmount = new BigDecimal("50.00");

        // When: Transfer 50 from Repartition to Compte
        financialOps.transferFromObjectif(testRepartition, testCompte1, transferAmount);

        // Then: Repartition should have 50, Compte should have 1050
        assertEquals(
            initialRepartitionAmount.subtract(transferAmount),
            testRepartition.getMontantActuel()
        );
        assertEquals(
            initialCompteBalance.add(transferAmount),
            testCompte1.getSoldeTotal()
        );
        assertEquals(new BigDecimal("50.00"), testRepartition.getMontantActuel());
        assertEquals(new BigDecimal("1050.00"), testCompte1.getSoldeTotal());
    }

    @Test
    @TestTransaction
    void transferFromObjectif_ShouldThrowException_WhenInsufficientFundsInRepartition() {
        // Given: Repartition with only 100
        testRepartition.setMontantActuel(new BigDecimal("100.00"));

        // When/Then: Transfer 150 should throw exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> financialOps.transferFromObjectif(
                testRepartition, testCompte1, new BigDecimal("150.00")
            )
        );
        assertEquals("Insufficient funds in objectif repartition", exception.getMessage());
    }

    @Test
    @TestTransaction
    void transferFromObjectif_ShouldThrowException_WhenAmountIsZero() {
        // When/Then: Transfer with zero amount should throw exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> financialOps.transferFromObjectif(testRepartition, testCompte1, BigDecimal.ZERO)
        );
        assertEquals("Montant must be positive", exception.getMessage());
    }

    @Test
    @TestTransaction
    void transferFromObjectif_ShouldThrowException_WhenAmountIsNegative() {
        // When/Then: Transfer with negative amount should throw exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> financialOps.transferFromObjectif(
                testRepartition, testCompte1, new BigDecimal("-25.00")
            )
        );
        assertEquals("Montant must be positive", exception.getMessage());
    }

    // ========== CALCULATION HELPER TESTS ==========

    @Test
    @TestTransaction
    void calculateObjectifTotal_ShouldSumAllRepartitions() {
        // Given: Objectif with multiple repartitions
        ObjectifRepartition rep2 = new ObjectifRepartition(testObjectif, testCompte2,
            new BigDecimal("200.00"));
        rep2.setOrdre(2);
        rep2.persist();

        // Flush and reload with JOIN FETCH to load repartitions
        entityManager.flush();
        Objectif objectifWithReps = entityManager.createQuery(
            "SELECT o FROM Objectif o LEFT JOIN FETCH o.repartitions WHERE o.id = :id", Objectif.class)
            .setParameter("id", testObjectif.getId())
            .getSingleResult();

        // When: Calculate total
        BigDecimal total = financialOps.calculateObjectifTotal(objectifWithReps);

        // Then: Should sum 100 + 200 = 300
        assertEquals(new BigDecimal("300.00"), total);
    }

    @Test
    @TestTransaction
    void calculateObjectifTotal_ShouldReturnZero_WhenNoRepartitions() {
        // Given: Objectif with no repartitions
        Objectif emptyObjectif = new Objectif(testUser, "Empty",
            new BigDecimal("1000.00"), PrioriteObjectif.BASSE, TypeObjectif.DIVERS);
        emptyObjectif.persist();

        // Flush and reload with JOIN FETCH to load repartitions (empty collection)
        entityManager.flush();
        Objectif objectifWithReps = entityManager.createQuery(
            "SELECT o FROM Objectif o LEFT JOIN FETCH o.repartitions WHERE o.id = :id", Objectif.class)
            .setParameter("id", emptyObjectif.getId())
            .getSingleResult();

        // When: Calculate total
        BigDecimal total = financialOps.calculateObjectifTotal(objectifWithReps);

        // Then: Should be zero
        assertEquals(MoneyConstants.ZERO, total);
    }

    @Test
    @TestTransaction
    void calculateObjectifProgress_ShouldCalculatePercentage() {
        // Given: Objectif with target 10000 and current 2500
        testObjectif.setMontantCible(new BigDecimal("10000.00"));
        testRepartition.setMontantActuel(new BigDecimal("2500.00"));

        // Flush and reload with JOIN FETCH to load repartitions
        entityManager.flush();
        Objectif objectifWithReps = entityManager.createQuery(
            "SELECT o FROM Objectif o LEFT JOIN FETCH o.repartitions WHERE o.id = :id", Objectif.class)
            .setParameter("id", testObjectif.getId())
            .getSingleResult();

        // When: Calculate progress
        BigDecimal progress = financialOps.calculateObjectifProgress(objectifWithReps);

        // Then: Should be 25%
        assertEquals(new BigDecimal("25.00"), progress);
    }

    @Test
    @TestTransaction
    void calculateObjectifProgress_ShouldReturnZero_WhenTargetIsZero() {
        // Given: Objectif with zero target
        testObjectif.setMontantCible(BigDecimal.ZERO);

        // When: Calculate progress
        BigDecimal progress = financialOps.calculateObjectifProgress(testObjectif);

        // Then: Should return zero (avoid division by zero)
        assertEquals(MoneyConstants.ZERO, progress);
    }

    @Test
    @TestTransaction
    void calculateObjectifProgress_ShouldExceed100_WhenOverfunded() {
        // Given: Objectif with target 1000 but current 1500
        testObjectif.setMontantCible(new BigDecimal("1000.00"));
        testRepartition.setMontantActuel(new BigDecimal("1500.00"));

        // Flush and reload with JOIN FETCH to load repartitions
        entityManager.flush();
        Objectif objectifWithReps = entityManager.createQuery(
            "SELECT o FROM Objectif o LEFT JOIN FETCH o.repartitions WHERE o.id = :id", Objectif.class)
            .setParameter("id", testObjectif.getId())
            .getSingleResult();

        // When: Calculate progress
        BigDecimal progress = financialOps.calculateObjectifProgress(objectifWithReps);

        // Then: Should be 150%
        assertEquals(new BigDecimal("150.00"), progress);
    }

    @Test
    @TestTransaction
    void hasSufficientFunds_ShouldReturnTrue_WhenFundsAreSufficient() {
        // Given: Compte with 1000
        testCompte1.setSoldeTotal(new BigDecimal("1000.00"));

        // When/Then: Check for 500
        assertTrue(financialOps.hasSufficientFunds(testCompte1, new BigDecimal("500.00")));
    }

    @Test
    @TestTransaction
    void hasSufficientFunds_ShouldReturnTrue_WhenAmountIsExact() {
        // Given: Compte with 1000
        testCompte1.setSoldeTotal(new BigDecimal("1000.00"));

        // When/Then: Check for exact amount
        assertTrue(financialOps.hasSufficientFunds(testCompte1, new BigDecimal("1000.00")));
    }

    @Test
    @TestTransaction
    void hasSufficientFunds_ShouldReturnFalse_WhenFundsAreInsufficient() {
        // Given: Compte with 100
        testCompte1.setSoldeTotal(new BigDecimal("100.00"));

        // When/Then: Check for 500
        assertFalse(financialOps.hasSufficientFunds(testCompte1, new BigDecimal("500.00")));
    }

    @Test
    @TestTransaction
    void hasSufficientFunds_ShouldReturnTrue_WhenCheckingZero() {
        // Given: Compte with any balance
        testCompte1.setSoldeTotal(new BigDecimal("100.00"));

        // When/Then: Check for zero
        assertTrue(financialOps.hasSufficientFunds(testCompte1, BigDecimal.ZERO));
    }

    // ========== ROUNDING TESTS ==========

    @Test
    @TestTransaction
    void moneyOperations_ShouldRoundConsistently() {
        // Given: Compte with precise starting balance
        testCompte1.setSoldeTotal(new BigDecimal("1000.00"));

        // When: Perform operations with amounts requiring rounding
        financialOps.debitCompte(testCompte1, new BigDecimal("333.333"));  // 1000 - 333.33 = 666.67
        financialOps.creditCompte(testCompte1, new BigDecimal("111.111")); // 666.67 + 111.11 = 777.78

        // Then: Result should be properly rounded
        assertEquals(new BigDecimal("777.78"), testCompte1.getSoldeTotal());
    }

    @Test
    @TestTransaction
    void moneyOperations_ShouldHandleManySmallOperations() {
        // Given: Compte with balance
        testCompte1.setSoldeTotal(new BigDecimal("100.00"));

        // When: Perform many small operations
        for (int i = 0; i < 10; i++) {
            financialOps.creditCompte(testCompte1, new BigDecimal("0.10"));
        }

        // Then: Should accumulate correctly
        assertEquals(new BigDecimal("101.00"), testCompte1.getSoldeTotal());
    }
}
