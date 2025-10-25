package com.budgetmanager.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkCreateTransactionRequest {
    @NotNull(message = "Le compte est obligatoire")
    private UUID compteId;

    @NotNull(message = "La liste de transactions est obligatoire")
    @NotEmpty(message = "La liste de transactions ne peut pas être vide")
    @Valid
    private List<CreateTransactionRequest> transactions;
}
