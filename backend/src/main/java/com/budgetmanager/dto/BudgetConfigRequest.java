package com.budgetmanager.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetConfigRequest {
    @NotNull(message = "Le pourcentage charges fixes est obligatoire")
    @DecimalMin(value = "0.0", message = "Le pourcentage doit être entre 0 et 100")
    @DecimalMax(value = "100.0", message = "Le pourcentage doit être entre 0 et 100")
    private BigDecimal pourcentageChargesFixes;

    @NotNull(message = "Le pourcentage dépenses variables est obligatoire")
    @DecimalMin(value = "0.0", message = "Le pourcentage doit être entre 0 et 100")
    @DecimalMax(value = "100.0", message = "Le pourcentage doit être entre 0 et 100")
    private BigDecimal pourcentageDepensesVariables;

    @NotNull(message = "Le pourcentage épargne est obligatoire")
    @DecimalMin(value = "0.0", message = "Le pourcentage doit être entre 0 et 100")
    @DecimalMax(value = "100.0", message = "Le pourcentage doit être entre 0 et 100")
    private BigDecimal pourcentageEpargne;

    // Validation dans le service : total doit = 100%
}
