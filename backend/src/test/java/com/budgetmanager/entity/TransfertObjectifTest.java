package com.budgetmanager.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

class TransfertObjectifTest {

    private TransfertObjectif transfert;

    @Mock
    private User mockUser;
    @Mock
    private Objectif mockObjectifSource;
    @Mock
    private Objectif mockObjectifDestination;
    @Mock
    private Compte mockCompteSource;
    @Mock
    private Compte mockCompteDestination;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transfert = new TransfertObjectif();
    }

    @Test
    void testTransfertCreationWithConstructor() {
        // Given
        BigDecimal montant = new BigDecimal("500.00");
        String motif = "Réajustement des priorités";

        // When
        TransfertObjectif transfert = new TransfertObjectif(
                mockUser, mockObjectifSource, mockObjectifDestination,
                mockCompteSource, mockCompteDestination, montant, motif
        );

        // Then
        assertEquals(mockUser, transfert.getUser());
        assertEquals(mockObjectifSource, transfert.getObjectifSource());
        assertEquals(mockObjectifDestination, transfert.getObjectifDestination());
        assertEquals(mockCompteSource, transfert.getCompteSource());
        assertEquals(mockCompteDestination, transfert.getCompteDestination());
        assertEquals(montant, transfert.getMontant());
        assertEquals(motif, transfert.getMotif());
        assertEquals(LocalDate.now(), transfert.getDateTransfert());
    }

    @Test
    void testTransfertDefaultConstructor() {
        // When
        TransfertObjectif transfert = new TransfertObjectif();

        // Then
        assertNotNull(transfert);
        assertNull(transfert.getUser());
        assertNull(transfert.getMontant());
        assertNull(transfert.getMotif());
        assertNull(transfert.getDateTransfert());
    }
}
