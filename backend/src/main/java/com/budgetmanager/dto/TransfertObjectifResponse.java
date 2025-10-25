package com.budgetmanager.dto;

import com.budgetmanager.entity.TransfertObjectif;
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
public class TransfertObjectifResponse {
    private UUID id;
    private ObjectifResponse objectifSource;
    private ObjectifResponse objectifDestination;
    private CompteResponse compteSource;
    private CompteResponse compteDestination;
    private BigDecimal montant;
    private String description;
    private LocalDate dateTransfert;
    private LocalDateTime createdAt;

    public static TransfertObjectifResponse fromEntity(TransfertObjectif transfert) {
        return TransfertObjectifResponse.builder()
                .id(transfert.getId())
                .objectifSource(transfert.getObjectifSource() != null ? ObjectifResponse.fromEntity(transfert.getObjectifSource()) : null)
                .objectifDestination(transfert.getObjectifDestination() != null ? ObjectifResponse.fromEntity(transfert.getObjectifDestination()) : null)
                .compteSource(transfert.getCompteSource() != null ? CompteResponse.fromEntity(transfert.getCompteSource()) : null)
                .compteDestination(transfert.getCompteDestination() != null ? CompteResponse.fromEntity(transfert.getCompteDestination()) : null)
                .montant(transfert.getMontant())
                .description(transfert.getMotif())
                .dateTransfert(transfert.getDateTransfert())
                .createdAt(transfert.getCreatedAt())
                .build();
    }
}
