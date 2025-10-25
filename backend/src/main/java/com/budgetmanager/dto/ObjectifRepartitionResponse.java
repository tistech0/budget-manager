package com.budgetmanager.dto;

import com.budgetmanager.entity.ObjectifRepartition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectifRepartitionResponse {
    private UUID id;
    private CompteResponse compte;
    private BigDecimal montantActuel;
    private BigDecimal pourcentageCible;
    private Integer ordre;

    public static ObjectifRepartitionResponse fromEntity(ObjectifRepartition repartition) {
        return ObjectifRepartitionResponse.builder()
                .id(repartition.getId())
                .compte(repartition.getCompte() != null ? CompteResponse.fromEntity(repartition.getCompte()) : null)
                .montantActuel(repartition.getMontantActuel())
                .pourcentageCible(repartition.getPourcentageCible())
                .ordre(repartition.getOrdre())
                .build();
    }
}
