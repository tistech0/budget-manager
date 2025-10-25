package com.budgetmanager.dto;

import com.budgetmanager.entity.PrioriteObjectif;
import com.budgetmanager.entity.TypeObjectif;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateObjectifRequest {
    @NotBlank(message = "Le nom de l'objectif est obligatoire")
    private String nom;

    @NotNull(message = "Le montant cible est obligatoire")
    @Positive(message = "Le montant cible doit être positif")
    private BigDecimal montantCible;

    @NotNull(message = "La priorité est obligatoire")
    private PrioriteObjectif priorite;

    @NotNull(message = "Le type d'objectif est obligatoire")
    private TypeObjectif type;

    private String couleur;

    private String icone;

    private String description;

    @Valid
    private List<InitialRepartition> repartitions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InitialRepartition {
        @NotBlank(message = "L'ID du compte est obligatoire")
        private String accountId;  // ID of the account (can be temp ID during onboarding)

        @NotNull(message = "Le montant est obligatoire")
        @PositiveOrZero(message = "Le montant doit être positif ou zéro")
        private BigDecimal montant;
    }
}
