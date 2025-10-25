package com.budgetmanager.entity;

public enum TypeObjectif {
    // Sécurité et urgence (priorité maximale)
    SECURITE,

    // Objectifs temporels
    COURT_TERME,        // 0-2 ans
    MOYEN_TERME,        // 2-5 ans
    LONG_TERME,         // 5+ ans

    // Catégories spécifiques
    PLAISIR,
    FAMILLE,
    FORMATION,
    INVESTISSEMENT,
    PROJET_IMMOBILIER,
    TRANSPORT,
    SANTE,
    TECHNOLOGIE,

    // Objectifs flexibles
    OPPORTUNITE,
    DIVERS
}