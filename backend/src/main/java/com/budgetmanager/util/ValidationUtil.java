package com.budgetmanager.util;

import com.budgetmanager.entity.Compte;
import com.budgetmanager.entity.Objectif;
import com.budgetmanager.entity.User;
import jakarta.ws.rs.NotFoundException;

/**
 * Utility class for common validation operations
 * Reduces code duplication across Resources
 */
public final class ValidationUtil {

    private ValidationUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Validate that user exists
     * @throws NotFoundException if user is null
     */
    public static void validateUser(User user) {
        if (user == null) {
            throw new NotFoundException("User not found");
        }
    }

    /**
     * Validate that compte exists and is active
     * @throws NotFoundException if compte is null or inactive
     */
    public static void validateCompte(Compte compte) {
        if (compte == null) {
            throw new NotFoundException("Compte not found");
        }
        if (!compte.getActif()) {
            throw new NotFoundException("Compte is inactive");
        }
    }

    /**
     * Validate that compte exists and is active, with custom message
     */
    public static void validateCompte(Compte compte, String message) {
        if (compte == null || !compte.getActif()) {
            throw new NotFoundException(message);
        }
    }

    /**
     * Validate that objectif exists and is active
     * @throws NotFoundException if objectif is null or inactive
     */
    public static void validateObjectif(Objectif objectif) {
        if (objectif == null) {
            throw new NotFoundException("Objectif not found");
        }
        if (!objectif.getActif()) {
            throw new NotFoundException("Objectif is inactive");
        }
    }

    /**
     * Validate that objectif exists and is active, with custom message
     */
    public static void validateObjectif(Objectif objectif, String message) {
        if (objectif == null || !objectif.getActif()) {
            throw new NotFoundException(message);
        }
    }

    /**
     * Validate that entity belongs to user
     */
    public static void validateOwnership(User entityUser, User currentUser, String entityType) {
        if (entityUser == null || !entityUser.getId().equals(currentUser.getId())) {
            throw new NotFoundException(entityType + " not found or access denied");
        }
    }

    /**
     * Validate compte belongs to user
     */
    public static void validateCompteOwnership(Compte compte, User user) {
        validateCompte(compte);
        validateOwnership(compte.getUser(), user, "Compte");
    }

    /**
     * Validate objectif belongs to user
     */
    public static void validateObjectifOwnership(Objectif objectif, User user) {
        validateObjectif(objectif);
        validateOwnership(objectif.getUser(), user, "Objectif");
    }
}
