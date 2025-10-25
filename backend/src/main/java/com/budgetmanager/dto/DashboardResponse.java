package com.budgetmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private String mois;
    private UserResponse user;
    private List<CompteResponse> comptes;
    private List<ObjectifResponse> objectifs;
    private Boolean salaireValide; // Indique si le salaire a été validé pour ce mois
    private LocalDateTime timestamp;
}
