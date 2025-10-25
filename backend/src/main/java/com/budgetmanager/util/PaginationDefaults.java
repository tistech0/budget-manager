package com.budgetmanager.util;

/**
 * Constants for pagination defaults
 */
public final class PaginationDefaults {

    private PaginationDefaults() {
        // Utility class - prevent instantiation
    }

    /**
     * Default limit for paginated results (100 items)
     */
    public static final int DEFAULT_LIMIT = 100;

    /**
     * Maximum limit for paginated results (1000 items)
     */
    public static final int MAX_LIMIT = 1000;

    /**
     * Default page number (0-indexed)
     */
    public static final int DEFAULT_PAGE = 0;
}
