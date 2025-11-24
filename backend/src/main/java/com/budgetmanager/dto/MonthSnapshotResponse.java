package com.budgetmanager.dto;

import com.budgetmanager.entity.MonthSnapshot;
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
public class MonthSnapshotResponse {
    private UUID id;
    private String month;
    private String cycleStart;
    private String cycleEnd;

    private BigDecimal totalRevenus;
    private BigDecimal totalChargesFixes;
    private BigDecimal totalDepensesVariables;
    private BigDecimal totalEpargne;
    private BigDecimal soldeCompteCourant;

    private BigDecimal budgetChargesFixes;
    private BigDecimal budgetDepensesVariables;
    private BigDecimal salaireMensuel;

    private Integer nombreTransactions;
    private Integer nombreChargesFixes;
    private Integer nombreDepensesVariables;

    private LocalDateTime createdAt;

    // Calculated fields
    private BigDecimal resteChargesFixes;
    private BigDecimal resteDepensesVariables;
    private Double pourcentageChargesFixes;
    private Double pourcentageDepensesVariables;

    public static MonthSnapshotResponse fromEntity(MonthSnapshot snapshot) {
        BigDecimal resteCharges = snapshot.getBudgetChargesFixes() != null
                ? snapshot.getBudgetChargesFixes().subtract(snapshot.getTotalChargesFixes())
                : BigDecimal.ZERO;

        BigDecimal resteDepenses = snapshot.getBudgetDepensesVariables() != null
                ? snapshot.getBudgetDepensesVariables().subtract(snapshot.getTotalDepensesVariables())
                : BigDecimal.ZERO;

        Double pctCharges = snapshot.getBudgetChargesFixes() != null && snapshot.getBudgetChargesFixes().compareTo(BigDecimal.ZERO) > 0
                ? snapshot.getTotalChargesFixes().divide(snapshot.getBudgetChargesFixes(), 4, java.math.RoundingMode.HALF_UP).doubleValue() * 100
                : 0.0;

        Double pctDepenses = snapshot.getBudgetDepensesVariables() != null && snapshot.getBudgetDepensesVariables().compareTo(BigDecimal.ZERO) > 0
                ? snapshot.getTotalDepensesVariables().divide(snapshot.getBudgetDepensesVariables(), 4, java.math.RoundingMode.HALF_UP).doubleValue() * 100
                : 0.0;

        return MonthSnapshotResponse.builder()
                .id(snapshot.getId())
                .month(snapshot.getMonth())
                .cycleStart(snapshot.getCycleStart())
                .cycleEnd(snapshot.getCycleEnd())
                .totalRevenus(snapshot.getTotalRevenus())
                .totalChargesFixes(snapshot.getTotalChargesFixes())
                .totalDepensesVariables(snapshot.getTotalDepensesVariables())
                .totalEpargne(snapshot.getTotalEpargne())
                .soldeCompteCourant(snapshot.getSoldeCompteCourant())
                .budgetChargesFixes(snapshot.getBudgetChargesFixes())
                .budgetDepensesVariables(snapshot.getBudgetDepensesVariables())
                .salaireMensuel(snapshot.getSalaireMensuel())
                .nombreTransactions(snapshot.getNombreTransactions())
                .nombreChargesFixes(snapshot.getNombreChargesFixes())
                .nombreDepensesVariables(snapshot.getNombreDepensesVariables())
                .createdAt(snapshot.getCreatedAt())
                .resteChargesFixes(resteCharges)
                .resteDepensesVariables(resteDepenses)
                .pourcentageChargesFixes(pctCharges)
                .pourcentageDepensesVariables(pctDepenses)
                .build();
    }
}
