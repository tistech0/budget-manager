package com.budgetmanager.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * CreateCompteTransfertRequest
 * Pour créer un transfert entre deux comptes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompteTransfertRequest {
    @NotNull(message = "Le compte source est obligatoire")
    private UUID compteSourceId;

    @NotNull(message = "Le compte destination est obligatoire")
    private UUID compteDestinationId;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal montant;

    private String description; // Optionnel

    private String dateTransfert; // Optionnel, format "YYYY-MM-DD"
}
