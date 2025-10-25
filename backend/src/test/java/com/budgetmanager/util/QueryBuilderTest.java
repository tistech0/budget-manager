package com.budgetmanager.util;

import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class QueryBuilderTest {

    @Test
    void testBasicWhere() {
        QueryBuilder builder = new QueryBuilder();
        builder.where("user.id = :userId", "userId", 123L);

        assertEquals("user.id = :userId", builder.getQuery());
        assertTrue(builder.hasConditions());

        Parameters params = builder.getParameters();
        assertEquals(123L, params.map().get("userId"));
    }

    @Test
    void testMultipleWhereConditions() {
        QueryBuilder builder = new QueryBuilder();
        builder.where("user.id = :userId", "userId", 123L)
               .where("actif = :actif", "actif", true);

        assertEquals("user.id = :userId AND actif = :actif", builder.getQuery());
        assertTrue(builder.hasConditions());

        Parameters params = builder.getParameters();
        assertEquals(123L, params.map().get("userId"));
        assertEquals(true, params.map().get("actif"));
    }

    @Test
    void testWhereWithoutParams() {
        QueryBuilder builder = new QueryBuilder();
        builder.where("deleted = false");

        assertEquals("deleted = false", builder.getQuery());
        assertTrue(builder.hasConditions());
        assertEquals(0, builder.getParameters().map().size());
    }

    @Test
    void testWhereOptional_WithValue() {
        QueryBuilder builder = new QueryBuilder();
        builder.whereOptional("compte.id = :compteId", "compteId", 456L);

        assertEquals("compte.id = :compteId", builder.getQuery());
        assertTrue(builder.hasConditions());
        assertEquals(456L, builder.getParameters().map().get("compteId"));
    }

    @Test
    void testWhereOptional_WithNullValue() {
        QueryBuilder builder = new QueryBuilder();
        builder.whereOptional("compte.id = :compteId", "compteId", null);

        assertEquals("", builder.getQuery());
        assertFalse(builder.hasConditions());
        assertEquals(0, builder.getParameters().map().size());
    }

    @Test
    void testWhereDateBetween_BothDates() {
        QueryBuilder builder = new QueryBuilder();
        LocalDate debut = LocalDate.of(2025, 1, 1);
        LocalDate fin = LocalDate.of(2025, 1, 31);

        builder.whereDateBetween("dateTransaction", debut, fin);

        assertEquals("dateTransaction BETWEEN :dateDebut AND :dateFin", builder.getQuery());
        assertEquals(debut, builder.getParameters().map().get("dateDebut"));
        assertEquals(fin, builder.getParameters().map().get("dateFin"));
    }

    @Test
    void testWhereDateBetween_OnlyStartDate() {
        QueryBuilder builder = new QueryBuilder();
        LocalDate debut = LocalDate.of(2025, 1, 1);

        builder.whereDateBetween("dateTransaction", debut, null);

        assertEquals("dateTransaction >= :dateDebut", builder.getQuery());
        assertEquals(debut, builder.getParameters().map().get("dateDebut"));
        assertNull(builder.getParameters().map().get("dateFin"));
    }

    @Test
    void testWhereDateBetween_OnlyEndDate() {
        QueryBuilder builder = new QueryBuilder();
        LocalDate fin = LocalDate.of(2025, 1, 31);

        builder.whereDateBetween("dateTransaction", null, fin);

        assertEquals("dateTransaction <= :dateFin", builder.getQuery());
        assertEquals(fin, builder.getParameters().map().get("dateFin"));
        assertNull(builder.getParameters().map().get("dateDebut"));
    }

    @Test
    void testWhereDateBetween_NoDates() {
        QueryBuilder builder = new QueryBuilder();
        builder.whereDateBetween("dateTransaction", null, null);

        assertEquals("", builder.getQuery());
        assertFalse(builder.hasConditions());
    }

    @Test
    void testOrderByDesc() {
        QueryBuilder builder = new QueryBuilder();
        builder.orderByDesc("dateCreation");

        Sort sort = builder.getSort();
        assertNotNull(sort);
        assertEquals("dateCreation", sort.getColumns().get(0).getName());
        assertEquals(Sort.Direction.Descending, sort.getColumns().get(0).getDirection());
    }

    @Test
    void testOrderByAsc() {
        QueryBuilder builder = new QueryBuilder();
        builder.orderByAsc("nom");

        Sort sort = builder.getSort();
        assertNotNull(sort);
        assertEquals("nom", sort.getColumns().get(0).getName());
        assertEquals(Sort.Direction.Ascending, sort.getColumns().get(0).getDirection());
    }

    @Test
    void testOrderBy_Descending() {
        QueryBuilder builder = new QueryBuilder();
        builder.orderBy("priority", Sort.Direction.Descending);

        Sort sort = builder.getSort();
        assertNotNull(sort);
        assertEquals(Sort.Direction.Descending, sort.getColumns().get(0).getDirection());
    }

    @Test
    void testOrderBy_Ascending() {
        QueryBuilder builder = new QueryBuilder();
        builder.orderBy("priority", Sort.Direction.Ascending);

        Sort sort = builder.getSort();
        assertNotNull(sort);
        assertEquals(Sort.Direction.Ascending, sort.getColumns().get(0).getDirection());
    }

    @Test
    void testNoConditions() {
        QueryBuilder builder = new QueryBuilder();

        assertEquals("", builder.getQuery());
        assertFalse(builder.hasConditions());
        assertNull(builder.getSort());
    }

    @Test
    void testCombinedWhereAndSort() {
        QueryBuilder builder = new QueryBuilder();
        builder.where("user.id = :userId", "userId", 100L)
               .where("actif = true")
               .orderByDesc("dateCreation");

        assertEquals("user.id = :userId AND actif = true", builder.getQuery());
        assertTrue(builder.hasConditions());
        assertNotNull(builder.getSort());
        assertEquals(100L, builder.getParameters().map().get("userId"));
    }

    // ========== TransactionQueryBuilder Tests ==========

    @Test
    void testTransactionQueryBuilder_ForUser() {
        QueryBuilder.TransactionQueryBuilder builder = new QueryBuilder.TransactionQueryBuilder();
        builder.forUser(123L);

        assertEquals("user.id = :userId", builder.getQuery());
        assertEquals(123L, builder.getParameters().map().get("userId"));
    }

    @Test
    void testTransactionQueryBuilder_ForCompte() {
        QueryBuilder.TransactionQueryBuilder builder = new QueryBuilder.TransactionQueryBuilder();
        builder.forUser(123L)
               .forCompte(456L);

        assertEquals("user.id = :userId AND compte.id = :compteId", builder.getQuery());
        assertEquals(456L, builder.getParameters().map().get("compteId"));
    }

    @Test
    void testTransactionQueryBuilder_ForCompte_NullValue() {
        QueryBuilder.TransactionQueryBuilder builder = new QueryBuilder.TransactionQueryBuilder();
        builder.forUser(123L)
               .forCompte(null);

        assertEquals("user.id = :userId", builder.getQuery());
        assertNull(builder.getParameters().map().get("compteId"));
    }

    @Test
    void testTransactionQueryBuilder_ForObjectif() {
        QueryBuilder.TransactionQueryBuilder builder = new QueryBuilder.TransactionQueryBuilder();
        builder.forUser(123L)
               .forObjectif(789L);

        assertEquals("user.id = :userId AND objectif.id = :objectifId", builder.getQuery());
        assertEquals(789L, builder.getParameters().map().get("objectifId"));
    }

    @Test
    void testTransactionQueryBuilder_BetweenDates() {
        QueryBuilder.TransactionQueryBuilder builder = new QueryBuilder.TransactionQueryBuilder();
        LocalDate debut = LocalDate.of(2025, 1, 1);
        LocalDate fin = LocalDate.of(2025, 1, 31);

        builder.forUser(123L)
               .betweenDates(debut, fin);

        assertEquals("user.id = :userId AND dateTransaction BETWEEN :dateDebut AND :dateFin", builder.getQuery());
        assertEquals(debut, builder.getParameters().map().get("dateDebut"));
        assertEquals(fin, builder.getParameters().map().get("dateFin"));
    }

    @Test
    void testTransactionQueryBuilder_OfType() {
        QueryBuilder.TransactionQueryBuilder builder = new QueryBuilder.TransactionQueryBuilder();
        builder.forUser(123L)
               .ofType("SALAIRE");

        assertEquals("user.id = :userId AND type = :type", builder.getQuery());
        assertEquals("SALAIRE", builder.getParameters().map().get("type"));
    }

    @Test
    void testTransactionQueryBuilder_OfType_NullValue() {
        QueryBuilder.TransactionQueryBuilder builder = new QueryBuilder.TransactionQueryBuilder();
        builder.forUser(123L)
               .ofType(null);

        assertEquals("user.id = :userId", builder.getQuery());
        assertNull(builder.getParameters().map().get("type"));
    }

    @Test
    void testTransactionQueryBuilder_FullQuery() {
        QueryBuilder.TransactionQueryBuilder builder = new QueryBuilder.TransactionQueryBuilder();
        LocalDate debut = LocalDate.of(2025, 1, 1);
        LocalDate fin = LocalDate.of(2025, 1, 31);

        builder.forUser(123L)
               .forCompte(456L)
               .forObjectif(789L)
               .betweenDates(debut, fin)
               .ofType("EPARGNE")
               .orderByDesc("dateTransaction");

        String expected = "user.id = :userId AND compte.id = :compteId AND objectif.id = :objectifId " +
                         "AND dateTransaction BETWEEN :dateDebut AND :dateFin AND type = :type";
        assertEquals(expected, builder.getQuery());

        Parameters params = builder.getParameters();
        assertEquals(123L, params.map().get("userId"));
        assertEquals(456L, params.map().get("compteId"));
        assertEquals(789L, params.map().get("objectifId"));
        assertEquals(debut, params.map().get("dateDebut"));
        assertEquals(fin, params.map().get("dateFin"));
        assertEquals("EPARGNE", params.map().get("type"));

        assertNotNull(builder.getSort());
        assertEquals(Sort.Direction.Descending, builder.getSort().getColumns().get(0).getDirection());
    }

    // ========== TransfertQueryBuilder Tests ==========

    @Test
    void testTransfertQueryBuilder_ForUser() {
        QueryBuilder.TransfertQueryBuilder builder = new QueryBuilder.TransfertQueryBuilder();
        builder.forUser(100L);

        assertEquals("user.id = :userId", builder.getQuery());
        assertEquals(100L, builder.getParameters().map().get("userId"));
    }

    @Test
    void testTransfertQueryBuilder_ForCompteSource() {
        QueryBuilder.TransfertQueryBuilder builder = new QueryBuilder.TransfertQueryBuilder();
        builder.forUser(100L)
               .forCompteSource(200L);

        assertEquals("user.id = :userId AND compteSource.id = :compteSourceId", builder.getQuery());
        assertEquals(200L, builder.getParameters().map().get("compteSourceId"));
    }

    @Test
    void testTransfertQueryBuilder_ForCompteSource_NullValue() {
        QueryBuilder.TransfertQueryBuilder builder = new QueryBuilder.TransfertQueryBuilder();
        builder.forUser(100L)
               .forCompteSource(null);

        assertEquals("user.id = :userId", builder.getQuery());
        assertNull(builder.getParameters().map().get("compteSourceId"));
    }

    @Test
    void testTransfertQueryBuilder_ForObjectif() {
        QueryBuilder.TransfertQueryBuilder builder = new QueryBuilder.TransfertQueryBuilder();
        builder.forUser(100L)
               .forObjectif(300L);

        assertEquals("user.id = :userId AND objectif.id = :objectifId", builder.getQuery());
        assertEquals(300L, builder.getParameters().map().get("objectifId"));
    }

    @Test
    void testTransfertQueryBuilder_BetweenDates() {
        QueryBuilder.TransfertQueryBuilder builder = new QueryBuilder.TransfertQueryBuilder();
        LocalDate debut = LocalDate.of(2025, 2, 1);
        LocalDate fin = LocalDate.of(2025, 2, 28);

        builder.forUser(100L)
               .betweenDates(debut, fin);

        assertEquals("user.id = :userId AND dateTransfert BETWEEN :dateDebut AND :dateFin", builder.getQuery());
        assertEquals(debut, builder.getParameters().map().get("dateDebut"));
        assertEquals(fin, builder.getParameters().map().get("dateFin"));
    }

    @Test
    void testTransfertQueryBuilder_FullQuery() {
        QueryBuilder.TransfertQueryBuilder builder = new QueryBuilder.TransfertQueryBuilder();
        LocalDate debut = LocalDate.of(2025, 3, 1);
        LocalDate fin = LocalDate.of(2025, 3, 31);

        builder.forUser(100L)
               .forCompteSource(200L)
               .forObjectif(300L)
               .betweenDates(debut, fin)
               .orderByAsc("dateTransfert");

        String expected = "user.id = :userId AND compteSource.id = :compteSourceId AND objectif.id = :objectifId " +
                         "AND dateTransfert BETWEEN :dateDebut AND :dateFin";
        assertEquals(expected, builder.getQuery());

        Parameters params = builder.getParameters();
        assertEquals(100L, params.map().get("userId"));
        assertEquals(200L, params.map().get("compteSourceId"));
        assertEquals(300L, params.map().get("objectifId"));
        assertEquals(debut, params.map().get("dateDebut"));
        assertEquals(fin, params.map().get("dateFin"));

        assertNotNull(builder.getSort());
        assertEquals(Sort.Direction.Ascending, builder.getSort().getColumns().get(0).getDirection());
    }
}
