package com.budgetmanager.service;

import com.budgetmanager.entity.Compte;
import com.budgetmanager.entity.SalaireValide;
import com.budgetmanager.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service pour gérer les salaires validés par mois
 */
@ApplicationScoped
public class SalaireValideService {

    /**
     * Trouve un salaire validé pour un utilisateur et un mois donné
     *
     * @param user L'utilisateur
     * @param mois Le mois au format YYYY-MM
     * @return Optional contenant le salaire s'il existe
     */
    public Optional<SalaireValide> findByUserAndMois(User user, String mois) {
        return SalaireValide.find("user = ?1 and mois = ?2", user, mois).firstResultOptional();
    }

    /**
     * Vérifie si un salaire a déjà été validé pour un mois donné
     *
     * @param user L'utilisateur
     * @param mois Le mois au format YYYY-MM
     * @return true si un salaire existe pour ce mois
     */
    public boolean existsForMois(User user, String mois) {
        return SalaireValide.count("user = ?1 and mois = ?2", user, mois) > 0;
    }

    /**
     * Récupère tous les salaires validés d'un utilisateur
     *
     * @param user L'utilisateur
     * @return Liste des salaires validés
     */
    public List<SalaireValide> findAllByUser(User user) {
        return SalaireValide.list("user = ?1 order by mois desc", user);
    }

    /**
     * Crée ou met à jour un salaire validé pour un mois
     * Si un salaire existe déjà pour ce mois, il est mis à jour
     *
     * @param user L'utilisateur
     * @param mois Le mois au format YYYY-MM
     * @param montantSalaire Le montant du salaire
     * @param dateReception La date de réception
     * @param compte Le compte de réception
     * @param description La description
     * @return Le salaire validé créé ou mis à jour
     */
    @Transactional
    public SalaireValide createOrUpdate(User user, String mois, BigDecimal montantSalaire,
                                        LocalDate dateReception, Compte compte, String description) {
        Optional<SalaireValide> existingOpt = findByUserAndMois(user, mois);

        if (existingOpt.isPresent()) {
            // Mise à jour du salaire existant
            SalaireValide existing = existingOpt.get();
            existing.setMontantSalaire(montantSalaire);
            existing.setDateReception(dateReception);
            existing.setCompte(compte);
            existing.setDescription(description);
            existing.persist();
            return existing;
        } else {
            // Création d'un nouveau salaire
            SalaireValide nouveau = new SalaireValide(user, mois, montantSalaire, dateReception, compte, description);
            nouveau.persist();
            return nouveau;
        }
    }

    /**
     * Supprime un salaire validé
     *
     * @param salaireValide Le salaire à supprimer
     */
    @Transactional
    public void delete(SalaireValide salaireValide) {
        salaireValide.delete();
    }

    /**
     * Supprime un salaire validé par mois
     *
     * @param user L'utilisateur
     * @param mois Le mois au format YYYY-MM
     * @return true si un salaire a été supprimé
     */
    @Transactional
    public boolean deleteByMois(User user, String mois) {
        return SalaireValide.delete("user = ?1 and mois = ?2", user, mois) > 0;
    }
}
