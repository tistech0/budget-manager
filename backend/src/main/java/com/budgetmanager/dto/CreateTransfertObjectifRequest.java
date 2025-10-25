package com.budgetmanager.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * CreateTransfertObjectifRequest
 * Pour créer un transfert entre deux objectifs
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransfertObjectifRequest {
    @NotNull(message = "L'objectif source est obligatoire")
    private UUID objectifSourceId;

    @NotNull(message = "L'objectif destination est obligatoire")
    private UUID objectifDestinationId;

    @NotNull(message = "Le compte source est obligatoire")
    private UUID compteSourceId;

    @NotNull(message = "Le compte destination est obligatoire")
    private UUID compteDestinationId;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal montant;

    private String motif; // Optionnel
}
