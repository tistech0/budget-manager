package com.budgetmanager.util;

import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for building dynamic queries with Panache
 * Reduces code duplication in query construction
 */
public class QueryBuilder {

    private final StringBuilder query = new StringBuilder();
    private final Map<String, Object> params = new HashMap<>();
    private Sort sort;
    private boolean hasWhere = false;

    public QueryBuilder() {
    }

    /**
     * Add WHERE condition (or AND if WHERE already exists)
     */
    public QueryBuilder where(String condition, String paramName, Object paramValue) {
        if (hasWhere) {
            query.append(" AND ");
        } else {
            hasWhere = true;
        }
        query.append(condition);
        if (paramName != null && paramValue != null) {
            params.put(paramName, paramValue);
        }
        return this;
    }

    /**
     * Add WHERE condition without parameters
     */
    public QueryBuilder where(String condition) {
        return where(condition, null, null);
    }

    /**
     * Add optional WHERE condition (only if value is not null)
     */
    public QueryBuilder whereOptional(String condition, String paramName, Object paramValue) {
        if (paramValue != null) {
            return where(condition, paramName, paramValue);
        }
        return this;
    }

    /**
     * Add date range filter
     */
    public QueryBuilder whereDateBetween(String fieldName, LocalDate dateDebut, LocalDate dateFin) {
        if (dateDebut != null && dateFin != null) {
            where(fieldName + " BETWEEN :dateDebut AND :dateFin", null, null);
            params.put("dateDebut", dateDebut);
            params.put("dateFin", dateFin);
        } else if (dateDebut != null) {
            where(fieldName + " >= :dateDebut", "dateDebut", dateDebut);
        } else if (dateFin != null) {
            where(fieldName + " <= :dateFin", "dateFin", dateFin);
        }
        return this;
    }

    /**
     * Add sorting
     */
    public QueryBuilder orderBy(String field, Sort.Direction direction) {
        if (direction == Sort.Direction.Descending) {
            this.sort = Sort.descending(field);
        } else {
            this.sort = Sort.ascending(field);
        }
        return this;
    }

    /**
     * Add sorting (default descending)
     */
    public QueryBuilder orderByDesc(String field) {
        return orderBy(field, Sort.Direction.Descending);
    }

    /**
     * Add sorting (default ascending)
     */
    public QueryBuilder orderByAsc(String field) {
        return orderBy(field, Sort.Direction.Ascending);
    }

    /**
     * Get the built query string
     */
    public String getQuery() {
        return query.toString();
    }

    /**
     * Get Panache Parameters
     */
    public Parameters getParameters() {
        Parameters parameters = new Parameters();
        params.forEach((key, value) -> parameters.and(key, value));
        return parameters;
    }

    /**
     * Get Sort object
     */
    public Sort getSort() {
        return sort;
    }

    /**
     * Check if query has any conditions
     */
    public boolean hasConditions() {
        return hasWhere;
    }

    /**
     * Builder pattern for transaction queries
     */
    public static class TransactionQueryBuilder extends QueryBuilder {

        public TransactionQueryBuilder forUser(Long userId) {
            where("user.id = :userId", "userId", userId);
            return this;
        }

        public TransactionQueryBuilder forCompte(Long compteId) {
            whereOptional("compte.id = :compteId", "compteId", compteId);
            return this;
        }

        public TransactionQueryBuilder forObjectif(Long objectifId) {
            whereOptional("objectif.id = :objectifId", "objectifId", objectifId);
            return this;
        }

        public TransactionQueryBuilder betweenDates(LocalDate dateDebut, LocalDate dateFin) {
            whereDateBetween("dateTransaction", dateDebut, dateFin);
            return this;
        }

        public TransactionQueryBuilder ofType(String type) {
            whereOptional("type = :type", "type", type);
            return this;
        }
    }

    /**
     * Builder pattern for transfert queries
     */
    public static class TransfertQueryBuilder extends QueryBuilder {

        public TransfertQueryBuilder forUser(Long userId) {
            where("user.id = :userId", "userId", userId);
            return this;
        }

        public TransfertQueryBuilder forCompteSource(Long compteId) {
            whereOptional("compteSource.id = :compteSourceId", "compteSourceId", compteId);
            return this;
        }

        public TransfertQueryBuilder forObjectif(Long objectifId) {
            whereOptional("objectif.id = :objectifId", "objectifId", objectifId);
            return this;
        }

        public TransfertQueryBuilder betweenDates(LocalDate dateDebut, LocalDate dateFin) {
            whereDateBetween("dateTransfert", dateDebut, dateFin);
            return this;
        }
    }
}
