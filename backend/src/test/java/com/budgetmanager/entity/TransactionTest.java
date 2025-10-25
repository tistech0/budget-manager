package com.budgetmanager.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

class TransactionTest {

    private Transaction transaction;

    @Mock
    private User mockUser;
    @Mock
    private Compte mockCompte;
    @Mock
    private Objectif mockObjectif;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transaction = new Transaction();
    }

    @Test
    void testTransactionCreationSimple() {
        // Given
        BigDecimal montant = new BigDecimal("2500.00");
        TypeTransaction type = TypeTransaction.SALAIRE;
        String description = "Salaire de novembre";

        // When
        Transaction transaction = new Transaction(mockUser, mockCompte, montant, type, description);

        // Then
        assertEquals(mockUser, transaction.getUser());
        assertEquals(mockCompte, transaction.getCompte());
        assertEquals(montant, transaction.getMontant());
        assertEquals(type, transaction.getType());
        assertEquals(description, transaction.getDescription());
        assertEquals(LocalDate.now(), transaction.getDateTransaction());
    }

    @Test
    void testTransactionCreationWithObjectif() {
        // Given
        BigDecimal montant = new BigDecimal("300.00");
        TypeTransaction type = TypeTransaction.EPARGNE;
        String description = "Versement épargne sécurité";

        // When
        Transaction transaction = new Transaction(mockUser, mockCompte, mockObjectif, montant, type, description);

        // Then
        assertEquals(mockUser, transaction.getUser());
        assertEquals(mockCompte, transaction.getCompte());
        assertEquals(mockObjectif, transaction.getObjectif());
        assertEquals(montant, transaction.getMontant());
        assertEquals(type, transaction.getType());
        assertEquals(description, transaction.getDescription());
        assertEquals(LocalDate.now(), transaction.getDateTransaction());
    }
}
