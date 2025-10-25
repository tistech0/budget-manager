package com.budgetmanager.dto;

import com.budgetmanager.entity.Objectif;
import com.budgetmanager.entity.PrioriteObjectif;
import com.budgetmanager.entity.TypeObjectif;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectifResponse {
    private UUID id;
    private String nom;
    private BigDecimal montantCible;
    private String couleur;
    private String icone;
    private String description;
    private PrioriteObjectif priorite;
    private TypeObjectif type;
    private Boolean actif;

    // Calculated fields
    private BigDecimal montantActuel;
    private BigDecimal pourcentageProgression;
    private List<ObjectifRepartitionResponse> repartitions;

    public static ObjectifResponse fromEntity(Objectif objectif) {
        ObjectifResponse.ObjectifResponseBuilder builder = ObjectifResponse.builder()
                .id(objectif.getId())
                .nom(objectif.getNom())
                .montantCible(objectif.getMontantCible())
                .couleur(objectif.getCouleur())
                .icone(objectif.getIcone())
                .description(objectif.getDescription())
                .priorite(objectif.getPriorite())
                .type(objectif.getType())
                .actif(objectif.getActif());

        // Include calculated fields if they've been set by enrichirObjectif
        if (objectif.getMontantActuel() != null) {
            builder.montantActuel(objectif.getMontantActuel());
        }
        if (objectif.getPourcentageProgression() != null) {
            builder.pourcentageProgression(objectif.getPourcentageProgression());
        }
        if (objectif.getRepartitions() != null && !objectif.getRepartitions().isEmpty()) {
            builder.repartitions(
                objectif.getRepartitions().stream()
                    .map(ObjectifRepartitionResponse::fromEntity)
                    .collect(Collectors.toList())
            );
        }

        return builder.build();
    }

    public static ObjectifResponse fromEntityWithCalculations(
            Objectif objectif,
            BigDecimal montantActuel,
            BigDecimal pourcentageProgression
    ) {
        ObjectifResponse response = fromEntity(objectif);
        response.setMontantActuel(montantActuel);
        response.setPourcentageProgression(pourcentageProgression);

        if (objectif.getRepartitions() != null) {
            response.setRepartitions(
                objectif.getRepartitions().stream()
                    .map(ObjectifRepartitionResponse::fromEntity)
                    .collect(Collectors.toList())
            );
        }

        return response;
    }
}
