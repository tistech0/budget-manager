package com.budgetmanager.dto;

import com.budgetmanager.entity.FrequenceCharge;
import com.budgetmanager.entity.TypeTransaction;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateChargeFixeRequest {
    @NotNull(message = "Le compte est obligatoire")
    private UUID compteId;

    @NotBlank(message = "Le nom de la charge fixe est obligatoire")
    private String nom;

    private String description;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal montant;

    @NotNull(message = "La catégorie est obligatoire")
    private TypeTransaction categorie;

    @NotNull(message = "Le jour de prélèvement est obligatoire")
    @Min(value = 1, message = "Le jour de prélèvement doit être entre 1 et 31")
    @Max(value = 31, message = "Le jour de prélèvement doit être entre 1 et 31")
    private Integer jourPrelevement;

    @NotNull(message = "La fréquence est obligatoire")
    private FrequenceCharge frequence;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate dateDebut;

    private LocalDate dateFin; // Optionnel
}
