package com.budgetmanager.util;

import com.budgetmanager.entity.*;
import org.hibernate.Hibernate;

/**
 * Utility class for initializing lazy-loaded relationships
 * Reduces code duplication across Resources
 */
public final class LazyLoadingUtil {

    private LazyLoadingUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Initialize a Transaction with all its relationships
     */
    public static void initializeTransaction(Transaction transaction) {
        if (transaction == null) return;

        Hibernate.initialize(transaction.getCompte());
        if (transaction.getCompte() != null) {
            Hibernate.initialize(transaction.getCompte().getBanque());
        }
        if (transaction.getObjectif() != null) {
            Hibernate.initialize(transaction.getObjectif());
        }
        if (transaction.getTransfertObjectif() != null) {
            Hibernate.initialize(transaction.getTransfertObjectif());
        }
    }

    /**
     * Initialize a Compte with its Banque
     */
    public static void initializeCompte(Compte compte) {
        if (compte == null) return;

        if (compte.getBanque() != null) {
            Hibernate.initialize(compte.getBanque());
        }
    }

    /**
     * Initialize an Objectif with its repartitions
     */
    public static void initializeObjectif(Objectif objectif) {
        if (objectif == null) return;

        if (objectif.getRepartitions() != null) {
            Hibernate.initialize(objectif.getRepartitions());
            objectif.getRepartitions().forEach(rep -> {
                if (rep.getCompte() != null) {
                    Hibernate.initialize(rep.getCompte());
                }
            });
        }
    }

    /**
     * Initialize an ObjectifRepartition with its relationships
     */
    public static void initializeObjectifRepartition(ObjectifRepartition repartition) {
        if (repartition == null) return;

        if (repartition.getCompte() != null) {
            Hibernate.initialize(repartition.getCompte());
        }
        if (repartition.getObjectif() != null) {
            Hibernate.initialize(repartition.getObjectif());
        }
    }

    /**
     * Initialize a TransfertObjectif with all its relationships
     */
    public static void initializeTransfertObjectif(TransfertObjectif transfert) {
        if (transfert == null) return;

        Hibernate.initialize(transfert.getObjectifSource());
        Hibernate.initialize(transfert.getObjectifDestination());
        Hibernate.initialize(transfert.getCompteSource());
        Hibernate.initialize(transfert.getCompteDestination());
        if (transfert.getCompteSource() != null) {
            Hibernate.initialize(transfert.getCompteSource().getBanque());
        }
        if (transfert.getCompteDestination() != null) {
            Hibernate.initialize(transfert.getCompteDestination().getBanque());
        }
    }

    /**
     * Initialize a ChargeFixe with its relationships
     */
    public static void initializeChargeFixe(ChargeFixe chargeFixe) {
        if (chargeFixe == null) return;

        if (chargeFixe.getCompte() != null) {
            Hibernate.initialize(chargeFixe.getCompte());
            if (chargeFixe.getCompte().getBanque() != null) {
                Hibernate.initialize(chargeFixe.getCompte().getBanque());
            }
        }
    }
}
