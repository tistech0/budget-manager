package com.budgetmanager.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;

class CompteTest {

    private Compte compte;

    @Mock
    private User mockUser;

    @Mock
    private Banque mockBanque;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        compte = new Compte();
    }

    @Test
    void testCompteCreationWithConstructor() {
        // Given
        String nom = "Mon Livret A";
        TypeCompte type = TypeCompte.LIVRET_A;
        BigDecimal solde = new BigDecimal("5000.00");

        // When
        Compte compte = new Compte(mockUser, mockBanque, nom, type, solde);

        // Then
        assertEquals(mockUser, compte.getUser());
        assertEquals(mockBanque, compte.getBanque());
        assertEquals(nom, compte.getNom());
        assertEquals(type, compte.getType());
        assertEquals(solde, compte.getSoldeTotal());
        assertTrue(compte.getActif());
    }

    @Test
    void testGetArgentLibreWithoutObjectifs() {
        // Given
        compte.setSoldeTotal(new BigDecimal("1500.00"));
        compte.setObjectifRepartitions(null);

        // When
        BigDecimal argentLibre = compte.getArgentLibre();

        // Then
        assertEquals(new BigDecimal("1500.00"), argentLibre);
    }

    @Test
    void testGetArgentLibreWithObjectifs() {
        // Given
        compte.setSoldeTotal(new BigDecimal("1500.00"));

        ObjectifRepartition rep1 = new ObjectifRepartition();
        rep1.setMontantActuel(new BigDecimal("300.00"));

        ObjectifRepartition rep2 = new ObjectifRepartition();
        rep2.setMontantActuel(new BigDecimal("200.00"));

        compte.setObjectifRepartitions(Arrays.asList(rep1, rep2));

        // When
        BigDecimal argentLibre = compte.getArgentLibre();

        // Then
        assertEquals(new BigDecimal("1000.00"), argentLibre);
    }

    @Test
    void testGetArgentLibreWithEmptyList() {
        // Given
        compte.setSoldeTotal(new BigDecimal("1500.00"));
        compte.setObjectifRepartitions(Arrays.asList());

        // When
        BigDecimal argentLibre = compte.getArgentLibre();

        // Then
        assertEquals(new BigDecimal("1500.00"), argentLibre);
    }
}
