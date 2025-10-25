package com.budgetmanager.dto;

import com.budgetmanager.entity.Banque;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BanqueResponse {
    private UUID id;
    private String nom;
    private String couleurTheme;
    private String logoUrl;
    private Boolean actif;

    public static BanqueResponse fromEntity(Banque banque) {
        return BanqueResponse.builder()
                .id(banque.getId())
                .nom(banque.getNom())
                .couleurTheme(banque.getCouleurTheme())
                .logoUrl(banque.getLogoUrl())
                .actif(banque.getActif())
                .build();
    }
}
