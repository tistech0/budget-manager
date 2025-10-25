package com.budgetmanager.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

class ObjectifRepartitionTest {

    private ObjectifRepartition repartition;

    @Mock
    private Objectif mockObjectif;

    @Mock
    private Compte mockCompte;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repartition = new ObjectifRepartition();
    }

    @Test
    void testRepartitionCreationWithConstructor() {
        // Given
        BigDecimal montant = new BigDecimal("1500.00");

        // When
        ObjectifRepartition rep = new ObjectifRepartition(mockObjectif, mockCompte, montant);

        // Then
        assertEquals(mockObjectif, rep.getObjectif());
        assertEquals(mockCompte, rep.getCompte());
        assertEquals(montant, rep.getMontantActuel());
    }

    @Test
    void testRepartitionCreationWithPourcentage() {
        // Given
        BigDecimal montant = new BigDecimal("1500.00");
        BigDecimal pourcentage = new BigDecimal("30.00");

        // When
        ObjectifRepartition rep = new ObjectifRepartition(mockObjectif, mockCompte, montant, pourcentage);

        // Then
        assertEquals(mockObjectif, rep.getObjectif());
        assertEquals(mockCompte, rep.getCompte());
        assertEquals(montant, rep.getMontantActuel());
        assertEquals(pourcentage, rep.getPourcentageCible());
    }

    @Test
    void testRepartitionDefaultValues() {
        // When
        ObjectifRepartition rep = new ObjectifRepartition();

        // Then
        assertNotNull(rep);
        assertEquals(BigDecimal.ZERO, rep.getMontantActuel());
        assertEquals(Integer.valueOf(1), rep.getOrdre());
    }
}
