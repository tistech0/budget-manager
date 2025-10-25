package com.budgetmanager.dto;

import com.budgetmanager.util.MoneyConstants;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetConfigResponse {
    private BigDecimal pourcentageChargesFixes;
    private BigDecimal pourcentageDepensesVariables;
    private BigDecimal pourcentageEpargne;

    // Calculs automatiques basés sur le salaire
    private BigDecimal montantChargesFixes;
    private BigDecimal montantDepensesVariables;
    private BigDecimal montantEpargne;
    private BigDecimal salaireMensuelNet;

    public BudgetConfigResponse(BigDecimal pourcentageChargesFixes, BigDecimal pourcentageDepensesVariables,
                                BigDecimal pourcentageEpargne, BigDecimal salaireMensuelNet) {
        this.pourcentageChargesFixes = pourcentageChargesFixes;
        this.pourcentageDepensesVariables = pourcentageDepensesVariables;
        this.pourcentageEpargne = pourcentageEpargne;
        this.salaireMensuelNet = salaireMensuelNet;

        // Calculs automatiques
        this.montantChargesFixes = salaireMensuelNet.multiply(pourcentageChargesFixes).divide(MoneyConstants.PERCENT_DIVISOR);
        this.montantDepensesVariables = salaireMensuelNet.multiply(pourcentageDepensesVariables).divide(MoneyConstants.PERCENT_DIVISOR);
        this.montantEpargne = salaireMensuelNet.multiply(pourcentageEpargne).divide(MoneyConstants.PERCENT_DIVISOR);
    }
}
