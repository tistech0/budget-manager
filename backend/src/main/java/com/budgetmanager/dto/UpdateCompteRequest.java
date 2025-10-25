package com.budgetmanager.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompteRequest {
    private String nom;

    private BigDecimal soldeTotal;

    @PositiveOrZero(message = "Le taux doit être positif ou zéro")
    private BigDecimal taux;

    @PositiveOrZero(message = "Le plafond doit être positif ou zéro")
    private BigDecimal plafond;

    private Boolean principalChargesFixes;
}
