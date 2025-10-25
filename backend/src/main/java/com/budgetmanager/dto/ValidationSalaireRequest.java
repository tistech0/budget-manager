package com.budgetmanager.dto;

import com.budgetmanager.entity.TypeTransaction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * ValidationSalaireRequest
 * Pour valider le salaire mensuel ou autres revenus (action rapide)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationSalaireRequest {
    @NotBlank(message = "Le mois est obligatoire")
    private String mois; // Format : "2025-01" ou "Janvier 2025"

    @Positive(message = "Le montant doit être positif")
    private BigDecimal montant; // Optionnel pour SALAIRE, requis pour PRIME/FREELANCE

    private String dateReception; // Optionnel, utilise LocalDate.now() par défaut

    private UUID compteId; // Optionnel, utilise compte principal charges fixes par défaut

    private TypeTransaction type; // SALAIRE, PRIME, FREELANCE (défaut: SALAIRE)

    private String description; // Optionnel, auto-généré si non fourni
}
