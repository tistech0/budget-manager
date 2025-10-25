package com.budgetmanager.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Arrays;

class ObjectifTest {

    private Objectif objectif;

    @Mock
    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectif = new Objectif();
    }

    @Test
    void testObjectifCreationWithFullConstructor() {
        // Given
        String nom = "Vacances";
        BigDecimal montantCible = new BigDecimal("3000.00");
        PrioriteObjectif priorite = PrioriteObjectif.NORMALE;
        TypeObjectif type = TypeObjectif.PLAISIR;

        // When
        Objectif objectif = new Objectif(mockUser, nom, montantCible, priorite, type);

        // Then
        assertEquals(mockUser, objectif.getUser());
        assertEquals(nom, objectif.getNom());
        assertEquals(montantCible, objectif.getMontantCible());
        assertEquals(priorite, objectif.getPriorite());
        assertEquals(type, objectif.getType());
        assertTrue(objectif.getActif());
    }

    @Test
    void testObjectifCreationWithSimpleConstructor() {
        // Given
        String nom = "Voiture";
        BigDecimal montantCible = new BigDecimal("15000.00");
        TypeObjectif type = TypeObjectif.TRANSPORT;

        // When
        Objectif objectif = new Objectif(mockUser, nom, montantCible, type);

        // Then
        assertEquals(mockUser, objectif.getUser());
        assertEquals(nom, objectif.getNom());
        assertEquals(montantCible, objectif.getMontantCible());
        assertEquals(PrioriteObjectif.NORMALE, objectif.getPriorite());
        assertEquals(type, objectif.getType());
        assertTrue(objectif.getActif());
    }

    @Test
    void testGetMontantActuelWithoutRepartitions() {
        // Given
        objectif.setRepartitions(null);

        // When
        BigDecimal montantActuel = objectif.getMontantActuel();

        // Then
        assertEquals(BigDecimal.ZERO, montantActuel);
    }

    @Test
    void testGetMontantActuelWithRepartitions() {
        // Given
        ObjectifRepartition rep1 = new ObjectifRepartition();
        rep1.setMontantActuel(new BigDecimal("1000.00"));

        ObjectifRepartition rep2 = new ObjectifRepartition();
        rep2.setMontantActuel(new BigDecimal("500.00"));

        objectif.setRepartitions(Arrays.asList(rep1, rep2));

        // When
        BigDecimal montantActuel = objectif.getMontantActuel();

        // Then
        assertEquals(new BigDecimal("1500.00"), montantActuel);
    }

    @Test
    void testGetPourcentageProgressionNormal() {
        // Given
        objectif.setMontantCible(new BigDecimal("5000.00"));

        ObjectifRepartition rep1 = new ObjectifRepartition();
        rep1.setMontantActuel(new BigDecimal("1500.00"));

        objectif.setRepartitions(Arrays.asList(rep1));

        // When
        BigDecimal pourcentage = objectif.getPourcentageProgression();

        // Then
        assertEquals(new BigDecimal("30.00"), pourcentage);
    }

    @Test
    void testGetPourcentageProgressionCibleZero() {
        // Given
        objectif.setMontantCible(BigDecimal.ZERO);

        ObjectifRepartition rep1 = new ObjectifRepartition();
        rep1.setMontantActuel(new BigDecimal("1000.00"));

        objectif.setRepartitions(Arrays.asList(rep1));

        // When
        BigDecimal pourcentage = objectif.getPourcentageProgression();

        // Then
        assertEquals(BigDecimal.ZERO, pourcentage);
    }

    @Test
    void testGetNiveauPriorite() {
        // Given
        objectif.setPriorite(PrioriteObjectif.CRITIQUE);

        // When
        int niveau = objectif.getNiveauPriorite();

        // Then
        assertEquals(1, niveau);
    }

    @Test
    void testGetPourcentageProgressionComplete() {
        // Given
        objectif.setMontantCible(new BigDecimal("1000.00"));

        ObjectifRepartition rep1 = new ObjectifRepartition();
        rep1.setMontantActuel(new BigDecimal("1000.00"));

        objectif.setRepartitions(Arrays.asList(rep1));

        // When
        BigDecimal pourcentage = objectif.getPourcentageProgression();

        // Then
        assertEquals(new BigDecimal("100.00"), pourcentage);
    }

    @Test
    void testGetPourcentageProgressionDepasse() {
        // Given
        objectif.setMontantCible(new BigDecimal("1000.00"));

        ObjectifRepartition rep1 = new ObjectifRepartition();
        rep1.setMontantActuel(new BigDecimal("1200.00"));

        objectif.setRepartitions(Arrays.asList(rep1));

        // When
        BigDecimal pourcentage = objectif.getPourcentageProgression();

        // Then
        assertEquals(new BigDecimal("120.00"), pourcentage);
    }
}
