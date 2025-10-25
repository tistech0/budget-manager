package com.budgetmanager.util;

import java.math.BigDecimal;

/**
 * Constants for default budget percentages and validation
 */
public final class BudgetDefaults {

    private BudgetDefaults() {
        // Utility class - prevent instantiation
    }

    /**
     * Default percentage for fixed charges (50%)
     */
    public static final BigDecimal DEFAULT_CHARGES_FIXES_PERCENTAGE = new BigDecimal("50.00");

    /**
     * Default percentage for variable expenses (30%)
     */
    public static final BigDecimal DEFAULT_DEPENSES_VARIABLES_PERCENTAGE = new BigDecimal("30.00");

    /**
     * Default percentage for savings (20%)
     */
    public static final BigDecimal DEFAULT_EPARGNE_PERCENTAGE = new BigDecimal("20.00");

    /**
     * Total percentage that all budget categories must sum to (100%)
     */
    public static final BigDecimal TOTAL_PERCENTAGE = new BigDecimal("100.00");

    /**
     * Validates that budget percentages sum to 100%
     *
     * @param charges Fixed charges percentage
     * @param depenses Variable expenses percentage
     * @param epargne Savings percentage
     * @throws IllegalStateException if total does not equal 100%
     */
    public static void validateBudgetPercentages(BigDecimal charges,
                                                   BigDecimal depenses,
                                                   BigDecimal epargne) {
        BigDecimal total = charges.add(depenses).add(epargne);
        if (total.compareTo(TOTAL_PERCENTAGE) != 0) {
            throw new IllegalStateException(
                    "Les pourcentages de budget doivent totaliser 100%. Total actuel: " + total + "%"
            );
        }
    }
}
