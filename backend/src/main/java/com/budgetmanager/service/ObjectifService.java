package com.budgetmanager.service;

import com.budgetmanager.entity.Objectif;
import com.budgetmanager.entity.ObjectifRepartition;
import com.budgetmanager.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Service pour gérer la logique métier des objectifs.
 */
@ApplicationScoped
public class ObjectifService {

    @PersistenceContext
    EntityManager entityManager;

    /**
     * Fetches all active objectifs for a user with their repartitions in a single query (avoids N+1).
     * Uses entity graph to eagerly load repartitions, comptes, and banques.
     *
     * @param user The user
     * @return List of objectifs with all relations loaded
     */
    public List<Objectif> findObjectifsWithRepartitions(User user) {
        TypedQuery<Objectif> query = entityManager.createQuery(
                "SELECT DISTINCT o FROM Objectif o WHERE o.user = :user AND o.actif = true ORDER BY o.priorite, o.nom",
                Objectif.class
        );
        query.setParameter("user", user);
        query.setHint("jakarta.persistence.fetchgraph",
                entityManager.getEntityGraph("Objectif.withRepartitions"));

        List<Objectif> objectifs = query.getResultList();

        // Calculate montantActuel and pourcentageProgression for each objectif
        objectifs.forEach(this::calculateObjectifMetrics);

        return objectifs;
    }

    /**
     * Enrichit un objectif avec ses répartitions et calcule montantActuel et pourcentageProgression.
     * Cette méthode charge toutes les relations nécessaires via entity graph (pas de Hibernate.initialize).
     *
     * @param objectif L'objectif à enrichir
     */
    public void enrichirObjectif(Objectif objectif) {
        // Charger les répartitions avec leurs comptes et banques via entity graph
        List<ObjectifRepartition> repartitions = entityManager
                .createQuery("SELECT r FROM ObjectifRepartition r WHERE r.objectif = :objectif ORDER BY r.ordre", ObjectifRepartition.class)
                .setParameter("objectif", objectif)
                .setHint("jakarta.persistence.fetchgraph", entityManager.getEntityGraph("ObjectifRepartition.full"))
                .getResultList();

        objectif.setRepartitions(repartitions);
        calculateObjectifMetrics(objectif);
    }

    /**
     * Calculate montantActuel and pourcentageProgression for an objectif.
     * Assumes repartitions are already loaded.
     *
     * @param objectif The objectif to calculate metrics for
     */
    private void calculateObjectifMetrics(Objectif objectif) {
        List<ObjectifRepartition> repartitions = objectif.getRepartitions();
        if (repartitions == null) {
            objectif.setMontantActuel(BigDecimal.ZERO);
            objectif.setPourcentageProgression(BigDecimal.ZERO);
            return;
        }

        // Calculer montantActuel
        objectif.setMontantActuel(repartitions.stream()
                .map(ObjectifRepartition::getMontantActuel)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // Calculer pourcentageProgression
        if (objectif.getMontantCible() != null && objectif.getMontantCible().compareTo(BigDecimal.ZERO) > 0) {
            objectif.setPourcentageProgression(objectif.getMontantActuel()
                    .divide(objectif.getMontantCible(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP));
        } else {
            objectif.setPourcentageProgression(BigDecimal.ZERO);
        }
    }
}
