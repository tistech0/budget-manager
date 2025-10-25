package com.budgetmanager.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnumsTest {

    @Test
    void testPrioriteObjectifNiveaux() {
        assertEquals(1, PrioriteObjectif.CRITIQUE.niveau);
        assertEquals(2, PrioriteObjectif.TRES_HAUTE.niveau);
        assertEquals(3, PrioriteObjectif.HAUTE.niveau);
        assertEquals(4, PrioriteObjectif.NORMALE.niveau);
        assertEquals(5, PrioriteObjectif.BASSE.niveau);
        assertEquals(6, PrioriteObjectif.TRES_BASSE.niveau);
        assertEquals(7, PrioriteObjectif.SUSPENDU.niveau);
    }

    @Test
    void testTypeCompteValues() {
        assertNotNull(TypeCompte.valueOf("COMPTE_COURANT"));
        assertNotNull(TypeCompte.valueOf("LIVRET_A"));
        assertNotNull(TypeCompte.valueOf("PEA"));
        assertNotNull(TypeCompte.valueOf("ASSURANCE_VIE"));
    }

    @Test
    void testTypeObjectifValues() {
        assertNotNull(TypeObjectif.valueOf("SECURITE"));
        assertNotNull(TypeObjectif.valueOf("COURT_TERME"));
        assertNotNull(TypeObjectif.valueOf("LONG_TERME"));
        assertNotNull(TypeObjectif.valueOf("PLAISIR"));
    }

    @Test
    void testTypeTransactionValues() {
        assertNotNull(TypeTransaction.valueOf("SALAIRE"));
        assertNotNull(TypeTransaction.valueOf("EPARGNE"));
        assertNotNull(TypeTransaction.valueOf("TRANSFERT_OBJECTIF"));
        assertNotNull(TypeTransaction.valueOf("RESTAURANT"));
    }
}
