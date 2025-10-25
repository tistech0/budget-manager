package com.budgetmanager.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for date operations
 * Reduces code duplication and provides centralized date handling
 */
public final class DateUtil {

    private DateUtil() {
        // Utility class - prevent instantiation
    }

    // Common formatters
    public static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    public static final DateTimeFormatter YEAR_MONTH = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * Parse a date string (ISO format: YYYY-MM-DD)
     * Returns null if string is null or invalid
     */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(dateString, ISO_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parse a date string or return default value
     */
    public static LocalDate parseDateOrDefault(String dateString, LocalDate defaultValue) {
        LocalDate parsed = parseDate(dateString);
        return parsed != null ? parsed : defaultValue;
    }

    /**
     * Parse a date string or return current date
     */
    public static LocalDate parseDateOrNow(String dateString) {
        return parseDateOrDefault(dateString, LocalDate.now());
    }

    /**
     * Parse a year-month string (format: YYYY-MM)
     * Returns null if string is null or invalid
     */
    public static YearMonth parseYearMonth(String yearMonthString) {
        if (yearMonthString == null || yearMonthString.isBlank()) {
            return null;
        }

        try {
            return YearMonth.parse(yearMonthString, YEAR_MONTH);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Format a date to ISO string (YYYY-MM-DD)
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(ISO_DATE);
    }

    /**
     * Format a YearMonth to string (YYYY-MM)
     */
    public static String formatYearMonth(YearMonth yearMonth) {
        if (yearMonth == null) {
            return null;
        }
        return yearMonth.format(YEAR_MONTH);
    }

    /**
     * Get first day of month from year-month string
     */
    public static LocalDate getFirstDayOfMonth(String yearMonthString) {
        YearMonth yearMonth = parseYearMonth(yearMonthString);
        return yearMonth != null ? yearMonth.atDay(1) : null;
    }

    /**
     * Get last day of month from year-month string
     */
    public static LocalDate getLastDayOfMonth(String yearMonthString) {
        YearMonth yearMonth = parseYearMonth(yearMonthString);
        return yearMonth != null ? yearMonth.atEndOfMonth() : null;
    }

    /**
     * Check if a date falls within a range (inclusive)
     */
    public static boolean isInRange(LocalDate date, LocalDate start, LocalDate end) {
        if (date == null) {
            return false;
        }
        if (start != null && date.isBefore(start)) {
            return false;
        }
        if (end != null && date.isAfter(end)) {
            return false;
        }
        return true;
    }

    /**
     * Get current year-month as string (YYYY-MM)
     */
    public static String getCurrentYearMonth() {
        return YearMonth.now().format(YEAR_MONTH);
    }

    /**
     * Get current date as ISO string (YYYY-MM-DD)
     */
    public static String getCurrentDate() {
        return LocalDate.now().format(ISO_DATE);
    }
}
