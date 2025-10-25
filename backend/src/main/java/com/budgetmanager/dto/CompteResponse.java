package com.budgetmanager.dto;

import com.budgetmanager.entity.Compte;
import com.budgetmanager.entity.TypeCompte;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompteResponse {
    private UUID id;
    private BanqueResponse banque;
    private String nom;
    private TypeCompte type;
    private BigDecimal soldeTotal;
    private BigDecimal taux;
    private BigDecimal plafond;
    private LocalDate dateOuverture;
    private Boolean actif;
    private Boolean principalChargesFixes;
    private BigDecimal argentLibre; // Calculated field

    public static CompteResponse fromEntity(Compte compte) {
        return CompteResponse.builder()
                .id(compte.getId())
                .banque(compte.getBanque() != null ? BanqueResponse.fromEntity(compte.getBanque()) : null)
                .nom(compte.getNom())
                .type(compte.getType())
                .soldeTotal(compte.getSoldeTotal())
                .taux(compte.getTaux())
                .plafond(compte.getPlafond())
                .dateOuverture(compte.getDateOuverture())
                .actif(compte.getActif())
                .principalChargesFixes(compte.getPrincipalChargesFixes())
                .build();
    }

    public static CompteResponse fromEntityWithArgentLibre(Compte compte, BigDecimal argentLibre) {
        CompteResponse response = fromEntity(compte);
        response.setArgentLibre(argentLibre);
        return response;
    }
}
