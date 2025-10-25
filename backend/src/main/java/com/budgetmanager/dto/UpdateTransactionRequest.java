package com.budgetmanager.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * UpdateTransactionRequest
 * Pour mettre à jour une transaction existante
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTransactionRequest {
    @Positive(message = "Le montant doit être positif")
    private BigDecimal montant;

    private String description;

    private String dateTransaction; // Format ISO : "2025-01-15"
}

