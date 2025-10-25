package com.budgetmanager.service;

import com.budgetmanager.entity.Compte;
import com.budgetmanager.entity.Objectif;
import com.budgetmanager.entity.ObjectifRepartition;
import com.budgetmanager.util.MoneyConstants;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;

/**
 * Service for financial operations (debit, credit, transfer)
 * Ensures consistency and provides audit trail
 */
@ApplicationScoped
public class FinancialOperationService {

    /**
     * Debit (subtract) amount from compte
     */
    @Transactional
    public void debitCompte(Compte compte, BigDecimal montant) {
        if (montant.compareTo(MoneyConstants.ZERO) <= 0) {
            throw new IllegalArgumentException("Montant must be positive");
        }

        BigDecimal nouveauSolde = compte.getSoldeTotal().subtract(montant);
        compte.setSoldeTotal(MoneyConstants.round(nouveauSolde));
        // No need to call persist() - entity is already managed and changes are auto-tracked
    }

    /**
     * Credit (add) amount to compte
     */
    @Transactional
    public void creditCompte(Compte compte, BigDecimal montant) {
        if (montant.compareTo(MoneyConstants.ZERO) <= 0) {
            throw new IllegalArgumentException("Montant must be positive");
        }

        BigDecimal nouveauSolde = compte.getSoldeTotal().add(montant);
        compte.setSoldeTotal(MoneyConstants.round(nouveauSolde));
        // No need to call persist() - entity is already managed and changes are auto-tracked
    }

    /**
     * Transfer amount between two comptes
     */
    @Transactional
    public void transferBetweenComptes(Compte source, Compte destination, BigDecimal montant) {
        if (montant.compareTo(MoneyConstants.ZERO) <= 0) {
            throw new IllegalArgumentException("Montant must be positive");
        }

        debitCompte(source, montant);
        creditCompte(destination, montant);
    }

    /**
     * Debit amount from objectif repartition
     */
    @Transactional
    public void debitObjectifRepartition(ObjectifRepartition repartition, BigDecimal montant) {
        if (montant.compareTo(MoneyConstants.ZERO) <= 0) {
            throw new IllegalArgumentException("Montant must be positive");
        }

        BigDecimal nouveauMontant = repartition.getMontantActuel().subtract(montant);
        if (nouveauMontant.compareTo(MoneyConstants.ZERO) < 0) {
            throw new IllegalArgumentException("Insufficient funds in objectif repartition");
        }

        repartition.setMontantActuel(MoneyConstants.round(nouveauMontant));
        // No need to call persist() - entity is already managed and changes are auto-tracked
    }

    /**
     * Credit amount to objectif repartition
     */
    @Transactional
    public void creditObjectifRepartition(ObjectifRepartition repartition, BigDecimal montant) {
        if (montant.compareTo(MoneyConstants.ZERO) <= 0) {
            throw new IllegalArgumentException("Montant must be positive");
        }

        BigDecimal nouveauMontant = repartition.getMontantActuel().add(montant);
        repartition.setMontantActuel(MoneyConstants.round(nouveauMontant));
        // No need to call persist() - entity is already managed and changes are auto-tracked
    }

    /**
     * Transfer from compte to objectif repartition
     * (Used when allocating money to an objective)
     */
    @Transactional
    public void transferToObjectif(Compte compte, ObjectifRepartition repartition, BigDecimal montant) {
        if (montant.compareTo(MoneyConstants.ZERO) <= 0) {
            throw new IllegalArgumentException("Montant must be positive");
        }

        // Perform the transfer
        debitCompte(compte, montant);
        creditObjectifRepartition(repartition, montant);
    }

    /**
     * Transfer from objectif repartition to compte
     * (Used when withdrawing money from an objective)
     */
    @Transactional
    public void transferFromObjectif(ObjectifRepartition repartition, Compte compte, BigDecimal montant) {
        if (montant.compareTo(MoneyConstants.ZERO) <= 0) {
            throw new IllegalArgumentException("Montant must be positive");
        }

        debitObjectifRepartition(repartition, montant);
        creditCompte(compte, montant);
    }

    /**
     * Calculate total amount in objectif across all repartitions
     */
    public BigDecimal calculateObjectifTotal(Objectif objectif) {
        return objectif.getRepartitions().stream()
            .map(ObjectifRepartition::getMontantActuel)
            .reduce(MoneyConstants.ZERO, BigDecimal::add);
    }

    /**
     * Calculate progress percentage for objectif
     */
    public BigDecimal calculateObjectifProgress(Objectif objectif) {
        if (objectif.getMontantCible().compareTo(MoneyConstants.ZERO) == 0) {
            return MoneyConstants.ZERO;
        }

        BigDecimal montantActuel = calculateObjectifTotal(objectif);
        return MoneyConstants.percentage(montantActuel, objectif.getMontantCible());
    }

    /**
     * Check if compte has sufficient funds
     */
    public boolean hasSufficientFunds(Compte compte, BigDecimal montant) {
        BigDecimal nouveauSolde = compte.getSoldeTotal().subtract(montant);
        return nouveauSolde.compareTo(MoneyConstants.ZERO) >= 0;
    }
}
