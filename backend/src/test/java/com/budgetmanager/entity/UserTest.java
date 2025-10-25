package com.budgetmanager.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void testUserCreationWithConstructor() {
        // Given
        String nom = "Durand";
        String prenom = "Jean";
        Integer jourPaie = 15;
        BigDecimal salaire = new BigDecimal("2500.00");
        BigDecimal decouvert = new BigDecimal("1000.00");

        // When
        User user = new User(nom, prenom, jourPaie, salaire, decouvert);

        // Then
        assertEquals(nom, user.getNom());
        assertEquals(prenom, user.getPrenom());
        assertEquals(jourPaie, user.getJourPaie());
        assertEquals(salaire, user.getSalaireMensuelNet());
        assertEquals(decouvert, user.getDecouvertAutorise());
    }

    @Test
    void testUserDefaultConstructor() {
        // When
        User user = new User();

        // Then
        assertNotNull(user);
        assertNull(user.getNom());
        assertNull(user.getPrenom());
        assertNull(user.getJourPaie());
    }

    @Test
    void testUserFieldsAssignment() {
        // Given
        user.setNom("Martin");
        user.setPrenom("Paul");
        user.setJourPaie(28);
        user.setSalaireMensuelNet(new BigDecimal("3000.00"));
        user.setDecouvertAutorise(new BigDecimal("500.00"));

        // Then
        assertEquals("Martin", user.getNom());
        assertEquals("Paul", user.getPrenom());
        assertEquals(28, user.getJourPaie());
        assertEquals(new BigDecimal("3000.00"), user.getSalaireMensuelNet());
        assertEquals(new BigDecimal("500.00"), user.getDecouvertAutorise());
    }
}
