package com.budgetmanager.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Constants for money/financial calculations
 * Reuse these instead of creating new BigDecimal instances
 */
public final class MoneyConstants {

    private MoneyConstants() {
        // Utility class - prevent instantiation
    }

    // Common values
    public static final BigDecimal ZERO = BigDecimal.ZERO;
    public static final BigDecimal ONE = BigDecimal.ONE;
    public static final BigDecimal TEN = BigDecimal.TEN;
    public static final BigDecimal HUNDRED = new BigDecimal("100.00");

    // Percentage calculations
    public static final BigDecimal PERCENT_DIVISOR = new BigDecimal("100");

    // Rounding mode for money calculations
    public static final RoundingMode MONEY_ROUNDING_MODE = RoundingMode.HALF_UP;
    public static final int MONEY_SCALE = 2;

    /**
     * Helper method to create a properly scaled BigDecimal for money
     */
    public static BigDecimal money(String value) {
        return new BigDecimal(value).setScale(MONEY_SCALE, MONEY_ROUNDING_MODE);
    }

    /**
     * Helper method to create a properly scaled BigDecimal for money
     */
    public static BigDecimal money(double value) {
        return BigDecimal.valueOf(value).setScale(MONEY_SCALE, MONEY_ROUNDING_MODE);
    }

    /**
     * Helper method to create a properly scaled BigDecimal for money
     */
    public static BigDecimal money(long value) {
        return BigDecimal.valueOf(value).setScale(MONEY_SCALE, MONEY_ROUNDING_MODE);
    }

    /**
     * Round a BigDecimal to money precision
     */
    public static BigDecimal round(BigDecimal value) {
        return value.setScale(MONEY_SCALE, MONEY_ROUNDING_MODE);
    }

    /**
     * Calculate percentage: (value / total) * 100
     */
    public static BigDecimal percentage(BigDecimal value, BigDecimal total) {
        if (total.compareTo(ZERO) == 0) {
            return ZERO;
        }
        return value.divide(total, 4, MONEY_ROUNDING_MODE)
                   .multiply(HUNDRED)
                   .setScale(MONEY_SCALE, MONEY_ROUNDING_MODE);
    }

    /**
     * Apply percentage to value: (value * percentage) / 100
     */
    public static BigDecimal applyPercentage(BigDecimal value, BigDecimal percentage) {
        return value.multiply(percentage)
                   .divide(HUNDRED, MONEY_SCALE, MONEY_ROUNDING_MODE);
    }
}
