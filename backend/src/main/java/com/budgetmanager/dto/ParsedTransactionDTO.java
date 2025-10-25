package com.budgetmanager.dto;

import com.budgetmanager.entity.TypeTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedTransactionDTO {
    private String date;
    private String description;
    private BigDecimal montant;
    private TypeTransaction type;
    private Boolean isDebit;
    private String rawLine; // For debugging
}
