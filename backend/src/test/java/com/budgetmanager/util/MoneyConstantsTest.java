package com.budgetmanager.util;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class MoneyConstantsTest {

    @Test
    void testMoneyFromString() {
        BigDecimal result = MoneyConstants.money("123.456");
        assertEquals(new BigDecimal("123.46"), result);
        assertEquals(2, result.scale());
    }

    @Test
    void testMoneyFromStringWithLessDecimals() {
        BigDecimal result = MoneyConstants.money("100.5");
        assertEquals(new BigDecimal("100.50"), result);
        assertEquals(2, result.scale());
    }

    @Test
    void testMoneyFromDouble() {
        BigDecimal result = MoneyConstants.money(123.456);
        assertEquals(new BigDecimal("123.46"), result);
        assertEquals(2, result.scale());
    }

    @Test
    void testMoneyFromDoubleRoundsCorrectly() {
        BigDecimal result = MoneyConstants.money(123.445);
        assertEquals(new BigDecimal("123.45"), result);
        assertEquals(2, result.scale());
    }

    @Test
    void testMoneyFromLong() {
        BigDecimal result = MoneyConstants.money(100L);
        assertEquals(new BigDecimal("100.00"), result);
        assertEquals(2, result.scale());
    }

    @Test
    void testMoneyFromZero() {
        BigDecimal result = MoneyConstants.money(0L);
        assertEquals(new BigDecimal("0.00"), result);
        assertEquals(2, result.scale());
    }

    @Test
    void testRound() {
        BigDecimal unrounded = new BigDecimal("123.456789");
        BigDecimal result = MoneyConstants.round(unrounded);
        assertEquals(new BigDecimal("123.46"), result);
        assertEquals(2, result.scale());
    }

    @Test
    void testRoundDown() {
        BigDecimal unrounded = new BigDecimal("123.444");
        BigDecimal result = MoneyConstants.round(unrounded);
        assertEquals(new BigDecimal("123.44"), result);
    }

    @Test
    void testRoundHalfUp() {
        BigDecimal unrounded = new BigDecimal("123.445");
        BigDecimal result = MoneyConstants.round(unrounded);
        // HALF_UP rounds .445 to .45
        assertEquals(new BigDecimal("123.45"), result);
    }

    @ParameterizedTest
    @CsvSource({
        "50.00, 100.00, 50.00",
        "25.00, 100.00, 25.00",
        "33.33, 100.00, 33.33",
        "75.50, 100.00, 75.50",
        "0.00, 100.00, 0.00",
        "100.00, 100.00, 100.00"
    })
    void testPercentageCalculation(String value, String total, String expected) {
        BigDecimal result = MoneyConstants.percentage(
            new BigDecimal(value),
            new BigDecimal(total)
        );
        assertEquals(new BigDecimal(expected), result);
    }

    @Test
    void testPercentageWithZeroTotal() {
        BigDecimal result = MoneyConstants.percentage(
            new BigDecimal("50.00"),
            BigDecimal.ZERO
        );
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testPercentageWithDecimalResult() {
        // 30 out of 90 = 33.33%
        BigDecimal result = MoneyConstants.percentage(
            new BigDecimal("30.00"),
            new BigDecimal("90.00")
        );
        assertEquals(new BigDecimal("33.33"), result);
    }

    @Test
    void testPercentageRounding() {
        // 1 out of 3 = 33.3333... -> rounds to 33.33%
        BigDecimal result = MoneyConstants.percentage(
            new BigDecimal("1.00"),
            new BigDecimal("3.00")
        );
        assertEquals(new BigDecimal("33.33"), result);
    }

    @ParameterizedTest
    @CsvSource({
        "1000.00, 50.00, 500.00",
        "1000.00, 30.00, 300.00",
        "1000.00, 20.00, 200.00",
        "2500.00, 10.00, 250.00",
        "100.00, 0.00, 0.00",
        "100.00, 100.00, 100.00"
    })
    void testApplyPercentage(String value, String percentage, String expected) {
        BigDecimal result = MoneyConstants.applyPercentage(
            new BigDecimal(value),
            new BigDecimal(percentage)
        );
        assertEquals(new BigDecimal(expected), result);
    }

    @Test
    void testApplyPercentageWithRounding() {
        // 1000 * 33.33% = 333.30
        BigDecimal result = MoneyConstants.applyPercentage(
            new BigDecimal("1000.00"),
            new BigDecimal("33.33")
        );
        assertEquals(new BigDecimal("333.30"), result);
        assertEquals(2, result.scale());
    }

    @Test
    void testApplyPercentageRoundsHalfUp() {
        // Edge case: ensure HALF_UP rounding is used
        BigDecimal result = MoneyConstants.applyPercentage(
            new BigDecimal("100.00"),
            new BigDecimal("33.335")
        );
        // 100 * 33.335 / 100 = 33.335 -> rounds to 33.34
        assertEquals(new BigDecimal("33.34"), result);
    }

    @Test
    void testConstants() {
        assertEquals(BigDecimal.ZERO, MoneyConstants.ZERO);
        assertEquals(BigDecimal.ONE, MoneyConstants.ONE);
        assertEquals(BigDecimal.TEN, MoneyConstants.TEN);
        assertEquals(new BigDecimal("100.00"), MoneyConstants.HUNDRED);
        assertEquals(new BigDecimal("100"), MoneyConstants.PERCENT_DIVISOR);
        assertEquals(RoundingMode.HALF_UP, MoneyConstants.MONEY_ROUNDING_MODE);
        assertEquals(2, MoneyConstants.MONEY_SCALE);
    }

    @Test
    void testPercentageAndApplyPercentageInverse() {
        // Test that percentage() and applyPercentage() are inverse operations
        BigDecimal originalValue = new BigDecimal("500.00");
        BigDecimal total = new BigDecimal("1000.00");

        // Calculate percentage: 500 out of 1000 = 50%
        BigDecimal percent = MoneyConstants.percentage(originalValue, total);
        assertEquals(new BigDecimal("50.00"), percent);

        // Apply that percentage back to total: 1000 * 50% = 500
        BigDecimal appliedValue = MoneyConstants.applyPercentage(total, percent);
        assertEquals(originalValue, appliedValue);
    }

    @Test
    void testBudgetPercentageScenario() {
        // Real-world scenario: 50/30/20 budget rule
        BigDecimal salaire = new BigDecimal("3000.00");

        BigDecimal chargesFixes = MoneyConstants.applyPercentage(salaire, new BigDecimal("50.00"));
        assertEquals(new BigDecimal("1500.00"), chargesFixes);

        BigDecimal depensesVariables = MoneyConstants.applyPercentage(salaire, new BigDecimal("30.00"));
        assertEquals(new BigDecimal("900.00"), depensesVariables);

        BigDecimal epargne = MoneyConstants.applyPercentage(salaire, new BigDecimal("20.00"));
        assertEquals(new BigDecimal("600.00"), epargne);

        // Verify total
        BigDecimal total = chargesFixes.add(depensesVariables).add(epargne);
        assertEquals(salaire, total);
    }
}
