package com.budgetmanager.service;

import com.budgetmanager.entity.*;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour ObjectifService.
 * Vérifie les calculs de montant actuel et pourcentage de progression.
 */
@QuarkusTest
class ObjectifServiceTest {

    @Inject
    ObjectifService objectifService;

    @Inject
    EntityManager entityManager;

    private User testUser;
    private Banque testBanque;
    private Compte testCompte1;
    private Compte testCompte2;
    private Objectif testObjectif;

    @BeforeEach
    @TestTransaction
    void setUp() {
        // Nettoyer la base de données
        TransfertObjectif.deleteAll();
        Transaction.deleteAll();
        ObjectifRepartition.deleteAll();
        Objectif.deleteAll();
        SalaireValide.deleteAll();
        ChargeFixe.deleteAll();
        Compte.deleteAll();
        User.deleteAll();
        Banque.deleteAll();

        // Créer les données de test
        testUser = new User("Test", "User", 15,
            new BigDecimal("2500.00"), new BigDecimal("500.00"));
        testUser.persist();

        testBanque = new Banque("Test Bank", "#FF0000", null);
        testBanque.persist();

        testCompte1 = new Compte(testUser, testBanque, "Compte Test 1",
            TypeCompte.LIVRET_A, new BigDecimal("5000.00"));
        testCompte1.persist();

        testCompte2 = new Compte(testUser, testBanque, "Compte Test 2",
            TypeCompte.COMPTE_COURANT, new BigDecimal("3000.00"));
        testCompte2.persist();

        testObjectif = new Objectif(testUser, "Epargne Test",
            new BigDecimal("10000.00"), PrioriteObjectif.HAUTE, TypeObjectif.SECURITE);
        testObjectif.setCouleur("#4CAF50");
        testObjectif.persist();
    }

    @Test
    @TestTransaction
    void testEnrichirObjectif_WithNoRepartitions() {
        // Quand: enrichir un objectif sans répartitions
        objectifService.enrichirObjectif(testObjectif);

        // Alors: montantActuel = 0 et pourcentageProgression = 0
        assertNotNull(testObjectif.getRepartitions());
        assertEquals(0, testObjectif.getRepartitions().size());
        assertEquals(BigDecimal.ZERO, testObjectif.getMontantActuel());
        assertEquals(new BigDecimal("0.00"), testObjectif.getPourcentageProgression());
    }

    @Test
    @TestTransaction
    void testEnrichirObjectif_WithSingleRepartition() {
        // Etant donné: une répartition sur un compte
        ObjectifRepartition rep = new ObjectifRepartition(testObjectif, testCompte1,
            new BigDecimal("2500.00"));
        rep.setOrdre(1);
        rep.persist();

        // Quand: enrichir l'objectif
        objectifService.enrichirObjectif(testObjectif);

        // Alors: montantActuel = 2500, pourcentageProgression = 25%
        assertNotNull(testObjectif.getRepartitions());
        assertEquals(1, testObjectif.getRepartitions().size());
        assertEquals(new BigDecimal("2500.00"), testObjectif.getMontantActuel());
        assertEquals(new BigDecimal("25.00"), testObjectif.getPourcentageProgression());
    }

    @Test
    @TestTransaction
    void testEnrichirObjectif_WithMultipleRepartitions() {
        // Etant donné: deux répartitions sur deux comptes
        ObjectifRepartition rep1 = new ObjectifRepartition(testObjectif, testCompte1,
            new BigDecimal("2500.00"));
        rep1.setOrdre(1);
        rep1.persist();

        ObjectifRepartition rep2 = new ObjectifRepartition(testObjectif, testCompte2,
            new BigDecimal("1500.00"));
        rep2.setOrdre(2);
        rep2.persist();

        // Quand: enrichir l'objectif
        objectifService.enrichirObjectif(testObjectif);

        // Alors: montantActuel = 4000, pourcentageProgression = 40%
        assertNotNull(testObjectif.getRepartitions());
        assertEquals(2, testObjectif.getRepartitions().size());
        assertEquals(new BigDecimal("4000.00"), testObjectif.getMontantActuel());
        assertEquals(new BigDecimal("40.00"), testObjectif.getPourcentageProgression());
    }

    @Test
    @TestTransaction
    void testEnrichirObjectif_WithFullyFundedObjectif() {
        // Etant donné: répartitions totalisant 100% de l'objectif
        ObjectifRepartition rep = new ObjectifRepartition(testObjectif, testCompte1,
            new BigDecimal("10000.00"));
        rep.setOrdre(1);
        rep.persist();

        // Quand: enrichir l'objectif
        objectifService.enrichirObjectif(testObjectif);

        // Alors: montantActuel = 10000, pourcentageProgression = 100%
        assertEquals(new BigDecimal("10000.00"), testObjectif.getMontantActuel());
        assertEquals(new BigDecimal("100.00"), testObjectif.getPourcentageProgression());
    }

    @Test
    @TestTransaction
    void testEnrichirObjectif_WithOverfundedObjectif() {
        // Etant donné: répartitions dépassant 100% de l'objectif
        ObjectifRepartition rep = new ObjectifRepartition(testObjectif, testCompte1,
            new BigDecimal("15000.00"));
        rep.setOrdre(1);
        rep.persist();

        // Quand: enrichir l'objectif
        objectifService.enrichirObjectif(testObjectif);

        // Alors: montantActuel = 15000, pourcentageProgression = 150%
        assertEquals(new BigDecimal("15000.00"), testObjectif.getMontantActuel());
        assertEquals(new BigDecimal("150.00"), testObjectif.getPourcentageProgression());
    }

    @Test
    @TestTransaction
    void testEnrichirObjectif_WithZeroTarget() {
        // Etant donné: un objectif avec montant cible = 0
        Objectif zeroTargetObjectif = new Objectif(testUser, "Objectif Zero",
            BigDecimal.ZERO, PrioriteObjectif.BASSE, TypeObjectif.DIVERS);
        zeroTargetObjectif.persist();

        ObjectifRepartition rep = new ObjectifRepartition(zeroTargetObjectif, testCompte1,
            new BigDecimal("100.00"));
        rep.setOrdre(1);
        rep.persist();

        // Quand: enrichir l'objectif
        objectifService.enrichirObjectif(zeroTargetObjectif);

        // Alors: pourcentageProgression = 0 (évite division par zéro)
        assertEquals(new BigDecimal("100.00"), zeroTargetObjectif.getMontantActuel());
        assertEquals(BigDecimal.ZERO, zeroTargetObjectif.getPourcentageProgression());
    }

    @Test
    @TestTransaction
    void testEnrichirObjectif_RepartitionsOrderedCorrectly() {
        // Etant donné: trois répartitions avec des ordres spécifiques
        ObjectifRepartition rep3 = new ObjectifRepartition(testObjectif, testCompte1,
            new BigDecimal("1000.00"));
        rep3.setOrdre(3);
        rep3.persist();

        ObjectifRepartition rep1 = new ObjectifRepartition(testObjectif, testCompte2,
            new BigDecimal("2000.00"));
        rep1.setOrdre(1);
        rep1.persist();

        Compte testCompte3 = new Compte(testUser, testBanque, "Compte Test 3",
            TypeCompte.PEL, new BigDecimal("4000.00"));
        testCompte3.persist();

        ObjectifRepartition rep2 = new ObjectifRepartition(testObjectif, testCompte3,
            new BigDecimal("1500.00"));
        rep2.setOrdre(2);
        rep2.persist();

        // Quand: enrichir l'objectif
        objectifService.enrichirObjectif(testObjectif);

        // Alors: répartitions sont ordonnées par ordre
        assertEquals(3, testObjectif.getRepartitions().size());
        assertEquals(1, testObjectif.getRepartitions().get(0).getOrdre());
        assertEquals(2, testObjectif.getRepartitions().get(1).getOrdre());
        assertEquals(3, testObjectif.getRepartitions().get(2).getOrdre());
    }

    @Test
    @TestTransaction
    void testEnrichirObjectif_WithDecimalAmounts() {
        // Etant donné: répartitions avec montants décimaux
        ObjectifRepartition rep1 = new ObjectifRepartition(testObjectif, testCompte1,
            new BigDecimal("2490.50"));
        rep1.setOrdre(1);
        rep1.persist();

        ObjectifRepartition rep2 = new ObjectifRepartition(testObjectif, testCompte2,
            new BigDecimal("1234.75"));
        rep2.setOrdre(2);
        rep2.persist();

        // Quand: enrichir l'objectif
        objectifService.enrichirObjectif(testObjectif);

        // Alors: calculs précis
        assertEquals(new BigDecimal("3725.25"), testObjectif.getMontantActuel());
        assertEquals(new BigDecimal("37.25"), testObjectif.getPourcentageProgression());
    }

    @Test
    @TestTransaction
    void testEnrichirObjectif_RepartitionsLoadComptes() {
        // Etant donné: une répartition
        ObjectifRepartition rep = new ObjectifRepartition(testObjectif, testCompte1,
            new BigDecimal("2500.00"));
        rep.setOrdre(1);
        rep.persist();

        // Quand: enrichir l'objectif
        objectifService.enrichirObjectif(testObjectif);

        // Alors: les comptes et banques sont chargés (pas de lazy loading exception)
        assertNotNull(testObjectif.getRepartitions().get(0).getCompte());
        assertNotNull(testObjectif.getRepartitions().get(0).getCompte().getBanque());
        assertEquals("Compte Test 1", testObjectif.getRepartitions().get(0).getCompte().getNom());
        assertEquals("Test Bank", testObjectif.getRepartitions().get(0).getCompte().getBanque().getNom());
    }
}
