package com.budgetmanager.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRepartitionRequest {
    @NotNull(message = "Le compte est obligatoire")
    private UUID compteId;

    @PositiveOrZero(message = "Le montant actuel doit être positif ou zéro")
    private BigDecimal montantActuel;

    @PositiveOrZero(message = "Le pourcentage cible doit être positif ou zéro")
    private BigDecimal pourcentageCible;

    @Positive(message = "L'ordre doit être positif")
    private Integer ordre;
}
