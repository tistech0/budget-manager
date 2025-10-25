package com.budgetmanager.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class BanqueTest {

    private Banque banque;

    @BeforeEach
    void setUp() {
        banque = new Banque();
    }

    @Test
    void testBanqueCreationWithConstructor() {
        // Given
        String nom = "Fortuneo";
        String couleur = "#FF6600";
        String logo = "/logos/fortuneo.png";

        // When
        Banque banque = new Banque(nom, couleur, logo);

        // Then
        assertEquals(nom, banque.getNom());
        assertEquals(couleur, banque.getCouleurTheme());
        assertEquals(logo, banque.getLogoUrl());
        assertTrue(banque.getActif());
    }

    @Test
    void testBanqueDefaultConstructor() {
        // When
        Banque banque = new Banque();

        // Then
        assertNotNull(banque);
        assertNull(banque.getNom());
        assertNull(banque.getCouleurTheme());
        assertNull(banque.getLogoUrl());
        assertTrue(banque.getActif());
    }

    @Test
    void testBanqueActivation() {
        // Given
        banque.setActif(false);

        // When
        banque.setActif(true);

        // Then
        assertTrue(banque.getActif());
    }
}
