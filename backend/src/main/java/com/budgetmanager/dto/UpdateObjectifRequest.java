package com.budgetmanager.dto;

import com.budgetmanager.entity.PrioriteObjectif;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateObjectifRequest {
    private String nom;

    @Positive(message = "Le montant cible doit être positif")
    private BigDecimal montantCible;

    private PrioriteObjectif priorite;

    private String couleur;

    private String icone;

    private String description;
}
