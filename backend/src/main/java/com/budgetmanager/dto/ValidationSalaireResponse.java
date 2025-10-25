package com.budgetmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * ValidationSalaireResponse
 * Réponse après validation du salaire
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationSalaireResponse {
    private UUID transactionId;
    private String message;
    private BigDecimal nouveauSolde;
}
