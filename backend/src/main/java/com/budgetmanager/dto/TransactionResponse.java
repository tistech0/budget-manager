package com.budgetmanager.dto;

import com.budgetmanager.entity.Transaction;
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
public class TransactionResponse {
    private UUID id;
    private CompteResponse compte;
    private ObjectifResponse objectif;
    private BigDecimal montant;
    private String description;
    private TypeTransaction type;
    private LocalDate dateTransaction;
    private LocalDateTime createdAt;

    public static TransactionResponse fromEntity(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .compte(transaction.getCompte() != null ? CompteResponse.fromEntity(transaction.getCompte()) : null)
                .objectif(transaction.getObjectif() != null ? ObjectifResponse.fromEntity(transaction.getObjectif()) : null)
                .montant(transaction.getMontant())
                .description(transaction.getDescription())
                .type(transaction.getType())
                .dateTransaction(transaction.getDateTransaction())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
