package com.budgetmanager.util;

import com.budgetmanager.entity.Compte;
import com.budgetmanager.entity.Objectif;
import com.budgetmanager.entity.User;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    // ========== validateUser Tests ==========

    @Test
    void testValidateUser_WithValidUser() {
        User user = createTestUser("John", "Doe");

        // Should not throw exception
        assertDoesNotThrow(() -> ValidationUtil.validateUser(user));
    }

    @Test
    void testValidateUser_WithNullUser() {
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            ValidationUtil.validateUser(null)
        );

        assertEquals("User not found", exception.getMessage());
    }

    // ========== validateCompte Tests ==========

    @Test
    void testValidateCompte_WithValidActiveCompte() {
        Compte compte = createTestCompte("Test Compte", true);

        // Should not throw exception
        assertDoesNotThrow(() -> ValidationUtil.validateCompte(compte));
    }

    @Test
    void testValidateCompte_WithNullCompte() {
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            ValidationUtil.validateCompte(null)
        );

        assertEquals("Compte not found", exception.getMessage());
    }

    @Test
    void testValidateCompte_WithInactiveCompte() {
        Compte compte = createTestCompte("Inactive Compte", false);

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            ValidationUtil.validateCompte(compte)
        );

        assertEquals("Compte is inactive", exception.getMessage());
    }

    @Test
    void testValidateCompte_WithCustomMessage_ValidCompte() {
        Compte compte = createTestCompte("Test Compte", true);

        // Should not throw exception
        assertDoesNotThrow(() -> ValidationUtil.validateCompte(compte, "Custom error"));
    }

    @Test
    void testValidateCompte_WithCustomMessage_NullCompte() {
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            ValidationUtil.validateCompte(null, "Custom compte error")
        );

        assertEquals("Custom compte error", exception.getMessage());
    }

    @Test
    void testValidateCompte_WithCustomMessage_InactiveCompte() {
        Compte compte = createTestCompte("Inactive", false);

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            ValidationUtil.validateCompte(compte, "This compte is unavailable")
        );

        assertEquals("This compte is unavailable", exception.getMessage());
    }

    // ========== validateObjectif Tests ==========

    @Test
    void testValidateObjectif_WithValidActiveObjectif() {
        Objectif objectif = createTestObjectif("Test Objectif", true);

        // Should not throw exception
        assertDoesNotThrow(() -> ValidationUtil.validateObjectif(objectif));
    }

    @Test
    void testValidateObjectif_WithNullObjectif() {
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            ValidationUtil.validateObjectif(null)
        );

        assertEquals("Objectif not found", exception.getMessage());
    }

    @Test
    void testValidateObjectif_WithInactiveObjectif() {
        Objectif objectif = createTestObjectif("Inactive Objectif", false);

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            ValidationUtil.validateObjectif(objectif)
        );

        assertEquals("Objectif is inactive", exception.getMessage());
    }

    @Test
    void testValidateObjectif_WithCustomMessage_ValidObjectif() {
        Objectif objectif = createTestObjectif("Test Objectif", true);

        // Should not throw exception
        assertDoesNotThrow(() -> ValidationUtil.validateObjectif(objectif, "Custom error"));
    }

    @Test
    void testValidateObjectif_WithCustomMessage_NullObjectif() {
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            ValidationUtil.validateObjectif(null, "Custom objectif error")
        );

        assertEquals("Custom objectif error", exception.getMessage());
    }

    @Test
    void testValidateObjectif_WithCustomMessage_InactiveObjectif() {
        Objectif objectif = createTestObjectif("Inactive", false);

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            ValidationUtil.validateObjectif(objectif, "This objectif is unavailable")
        );

        assertEquals("This objectif is unavailable", exception.getMessage());
    }

    // ========== validateOwnership Tests ==========

    @Test
    void testValidateOwnership_WithSameUser() {
        User user = createTestUser("John", "Doe");

        // Should not throw exception
        assertDoesNotThrow(() ->
            ValidationUtil.validateOwnership(user, user, "Entity")
        );
    }

    @Test
    void testValidateOwnership_WithDifferentUsers() {
        User owner = createTestUser("John", "Doe");
        User currentUser = createTestUser("Jane", "Smith");

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            ValidationUtil.validateOwnership(owner, currentUser, "Transaction")
        );

        assertEquals("Transaction not found or access denied", exception.getMessage());
    }

    @Test
    void testValidateOwnership_WithNullEntityUser() {
        User currentUser = createTestUser("John", "Doe");

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            ValidationUtil.validateOwnership(null, currentUser, "Document")
        );

        assertEquals("Document not found or access denied", exception.getMessage());
    }

    @Test
    void testValidateOwnership_CustomEntityType() {
        User user = createTestUser("John", "Doe");
        User otherUser = createTestUser("Other", "User");

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            ValidationUtil.validateOwnership(otherUser, user, "CustomEntity")
        );

        assertEquals("CustomEntity not found or access denied", exception.getMessage());
    }

    // ========== validateCompteOwnership Tests ==========

    @Test
    void testValidateCompteOwnership_WithValidOwnership() {
        User user = createTestUser("John", "Doe");
        Compte compte = createTestCompte("Test Compte", true);
        compte.setUser(user);

        // Should not throw exception
        assertDoesNotThrow(() ->
            ValidationUtil.validateCompteOwnership(compte, user)
        );
    }

    @Test
    void testValidateCompteOwnership_WithNullCompte() {
        User user = createTestUser("John", "Doe");

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            ValidationUtil.validateCompteOwnership(null, user)
        );

        assertEquals("Compte not found", exception.getMessage());
    }

    @Test
    void testValidateCompteOwnership_WithInactiveCompte() {
        User user = createTestUser("John", "Doe");
        Compte compte = createTestCompte("Inactive", false);
        compte.setUser(user);

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            ValidationUtil.validateCompteOwnership(compte, user)
        );

        assertEquals("Compte is inactive", exception.getMessage());
    }

    @Test
    void testValidateCompteOwnership_WithWrongOwner() {
        User owner = createTestUser("John", "Doe");
        User currentUser = createTestUser("Jane", "Smith");
        Compte compte = createTestCompte("Test", true);
        compte.setUser(owner);

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            ValidationUtil.validateCompteOwnership(compte, currentUser)
        );

        assertEquals("Compte not found or access denied", exception.getMessage());
    }

    // ========== validateObjectifOwnership Tests ==========

    @Test
    void testValidateObjectifOwnership_WithValidOwnership() {
        User user = createTestUser("John", "Doe");
        Objectif objectif = createTestObjectif("Test Objectif", true);
        objectif.setUser(user);

        // Should not throw exception
        assertDoesNotThrow(() ->
            ValidationUtil.validateObjectifOwnership(objectif, user)
        );
    }

    @Test
    void testValidateObjectifOwnership_WithNullObjectif() {
        User user = createTestUser("John", "Doe");

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            ValidationUtil.validateObjectifOwnership(null, user)
        );

        assertEquals("Objectif not found", exception.getMessage());
    }

    @Test
    void testValidateObjectifOwnership_WithInactiveObjectif() {
        User user = createTestUser("John", "Doe");
        Objectif objectif = createTestObjectif("Inactive", false);
        objectif.setUser(user);

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            ValidationUtil.validateObjectifOwnership(objectif, user)
        );

        assertEquals("Objectif is inactive", exception.getMessage());
    }

    @Test
    void testValidateObjectifOwnership_WithWrongOwner() {
        User owner = createTestUser("John", "Doe");
        User currentUser = createTestUser("Jane", "Smith");
        Objectif objectif = createTestObjectif("Test", true);
        objectif.setUser(owner);

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            ValidationUtil.validateObjectifOwnership(objectif, currentUser)
        );

        assertEquals("Objectif not found or access denied", exception.getMessage());
    }

    // ========== Helper Methods ==========

    private User createTestUser(String nom, String prenom) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setSalaireMensuelNet(new BigDecimal("3000.00"));
        return user;
    }

    private Compte createTestCompte(String nom, boolean actif) {
        Compte compte = new Compte();
        compte.setId(UUID.randomUUID());
        compte.setNom(nom);
        compte.setActif(actif);
        compte.setSoldeTotal(BigDecimal.ZERO);
        return compte;
    }

    private Objectif createTestObjectif(String nom, boolean actif) {
        Objectif objectif = new Objectif();
        objectif.setId(UUID.randomUUID());
        objectif.setNom(nom);
        objectif.setActif(actif);
        objectif.setMontantCible(new BigDecimal("5000.00"));
        return objectif;
    }
}
