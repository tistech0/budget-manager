package com.budgetmanager.dto;

import com.budgetmanager.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String nom;
    private String prenom;
    private Integer jourPaie;
    private BigDecimal salaireMensuelNet;
    private BigDecimal decouvertAutorise;
    private BigDecimal objectifCompteCourant;
    private BigDecimal pourcentageChargesFixes;
    private BigDecimal pourcentageDepensesVariables;
    private BigDecimal pourcentageEpargne;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .jourPaie(user.getJourPaie())
                .salaireMensuelNet(user.getSalaireMensuelNet())
                .decouvertAutorise(user.getDecouvertAutorise())
                .objectifCompteCourant(user.getObjectifCompteCourant())
                .pourcentageChargesFixes(user.getPourcentageChargesFixes())
                .pourcentageDepensesVariables(user.getPourcentageDepensesVariables())
                .pourcentageEpargne(user.getPourcentageEpargne())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
