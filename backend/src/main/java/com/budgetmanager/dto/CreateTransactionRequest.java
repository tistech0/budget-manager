package com.budgetmanager.dto;

import com.budgetmanager.entity.TypeTransaction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * CreateTransactionRequest
 * Pour créer une nouvelle transaction
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {
    @NotNull(message = "Le compte est obligatoire")
    private UUID compteId;

    private UUID objectifId; // Optionnel

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal montant;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotNull(message = "Le type de transaction est obligatoire")
    private TypeTransaction type;

    @NotBlank(message = "La date de transaction est obligatoire")
    private String dateTransaction; // Format ISO : "2025-01-15"
}
