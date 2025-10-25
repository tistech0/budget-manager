package com.budgetmanager.dto;

import com.budgetmanager.entity.ChargeFixe;
import com.budgetmanager.entity.FrequenceCharge;
import com.budgetmanager.entity.TypeTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargeFixeResponse {
    private UUID id;
    private String nom;
    private String description;
    private BigDecimal montant;
    private TypeTransaction categorie;
    private Integer jourPrelevement;
    private FrequenceCharge frequence;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Boolean actif;
    private CompteResponse compte;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ChargeFixeResponse fromEntity(ChargeFixe charge) {
        return ChargeFixeResponse.builder()
                .id(charge.getId())
                .nom(charge.getNom())
                .description(charge.getDescription())
                .montant(charge.getMontant())
                .categorie(charge.getCategorie())
                .jourPrelevement(charge.getJourPrelevement())
                .frequence(charge.getFrequence())
                .dateDebut(charge.getDateDebut())
                .dateFin(charge.getDateFin())
                .actif(charge.getActif())
                .compte(charge.getCompte() != null ? CompteResponse.fromEntity(charge.getCompte()) : null)
                .createdAt(charge.getCreatedAt())
                .updatedAt(charge.getUpdatedAt())
                .build();
    }
}
