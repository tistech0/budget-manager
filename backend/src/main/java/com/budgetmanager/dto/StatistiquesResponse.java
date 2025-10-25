package com.budgetmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO pour statistiques
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatistiquesResponse {
    private String dateDebut;
    private String dateFin;
    private BigDecimal totalRevenus;
    private BigDecimal totalChargesFixes;
    private BigDecimal totalDepensesVariables;
    private BigDecimal totalEpargne;
    private BigDecimal soldeNet;
    private Integer nombreTransactions;
}
