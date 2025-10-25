package com.budgetmanager.entity;

public enum TypeTransaction {
    // Revenus
    SALAIRE,
    PRIME,
    FREELANCE,
    ALLOCATION,
    REMBOURSEMENT,
    GAIN_INVESTISSEMENT,
    CADEAU_RECU,
    VENTE,

    // Charges fixes
    LOYER,
    ASSURANCE,
    ABONNEMENT,
    CREDIT_IMMOBILIER,
    CREDIT_CONSO,
    IMPOTS,
    MUTUELLE,

    // Dépenses variables
    ALIMENTATION,
    RESTAURANT,
    TRANSPORT,
    ESSENCE,
    SHOPPING,
    LOISIRS,
    SANTE,
    BEAUTE,
    MAISON,
    EDUCATION,
    VOYAGE,

    // Épargne et investissement
    EPARGNE,
    INVESTISSEMENT,
    VIREMENT_INTERNE,
    TRANSFERT_OBJECTIF,
    VERSEMENT_OBJECTIF,

    // Autres
    RETRAIT_ESPECES,
    FRAIS_BANCAIRE,
    COMMISSION,
    AUTRE
}
