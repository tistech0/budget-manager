package com.budgetmanager.entity;

public enum PrioriteObjectif {
    CRITIQUE(1),        // Épargne de sécurité, urgences
    TRES_HAUTE(2),      // Objectifs essentiels à court terme
    HAUTE(3),           // Objectifs importants
    NORMALE(4),         // Objectifs standards
    BASSE(5),           // Objectifs différables
    TRES_BASSE(6),      // Objectifs de confort
    SUSPENDU(7);        // Objectifs en pause

    public final int niveau;

    PrioriteObjectif(int niveau) {
        this.niveau = niveau;
    }
}