package com.budgetmanager.dto;

import com.budgetmanager.entity.TypeCompte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompteRequest {
    @NotNull(message = "La banque est obligatoire")
    private UUID banqueId;

    @NotBlank(message = "Le nom du compte est obligatoire")
    private String nom;

    @NotNull(message = "Le type de compte est obligatoire")
    private TypeCompte type;

    @NotNull(message = "Le solde total est obligatoire")
    private BigDecimal soldeTotal;

    @PositiveOrZero(message = "Le taux doit être positif ou zéro")
    private BigDecimal taux;

    @PositiveOrZero(message = "Le plafond doit être positif ou zéro")
    private BigDecimal plafond;

    private LocalDate dateOuverture;

    private Boolean principalChargesFixes;
}
