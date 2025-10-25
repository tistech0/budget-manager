package com.budgetmanager.dto;

import com.budgetmanager.entity.Compte;
import com.budgetmanager.entity.TypeCompte;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompteSimpleDto {
    private UUID id;
    private String nom;
    private TypeCompte type;
    private BigDecimal soldeTotal;
    private BigDecimal argentLibre;
    private String banqueNom;
    private String banqueCouleur;

    public static CompteSimpleDto from(Compte compte) {
        CompteSimpleDto dto = new CompteSimpleDto();
        dto.id = compte.getId();
        dto.nom = compte.getNom();
        dto.type = compte.getType();
        dto.soldeTotal = compte.getSoldeTotal();

        dto.argentLibre = compte.getArgentLibre();

        if (compte.getBanque() != null) {
            dto.banqueNom = compte.getBanque().getNom();
            dto.banqueCouleur = compte.getBanque().getCouleurTheme();
        }

        return dto;
    }
}
