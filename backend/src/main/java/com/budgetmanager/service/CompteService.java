package com.budgetmanager.service;

import com.budgetmanager.entity.Compte;
import com.budgetmanager.entity.ObjectifRepartition;
import com.budgetmanager.entity.TypeCompte;
import com.budgetmanager.entity.User;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service pour g�rer la logique m�tier des comptes.
 */
@ApplicationScoped
public class CompteService {

    /**
     * R�cup�re tous les comptes actifs d'un utilisateur.
     *
     * @param user L'utilisateur
     * @param page Num�ro de la page (0-index�)
     * @param limit Nombre maximum de r�sultats par page
     * @return Liste des comptes actifs
     */
    public List<Compte> getComptesActifs(User user, int page, int limit) {
        return Compte.find("user = ?1 and actif = true order by nom", user)
                .page(page, limit)
                .list();
    }

    /**
     * R�cup�re le compte principal pour les charges fixes.
     * Si aucun compte n'est marqu� comme principal, retourne le premier compte courant.
     *
     * @param user L'utilisateur
     * @return Le compte principal ou null si aucun compte
     */
    public Compte getComptePrincipalChargesFixes(User user) {
        // Chercher le compte marqu� comme principal
        Compte comptePrincipal = Compte.find(
                "user = ?1 and principalChargesFixes = true and actif = true",
                user
        ).firstResult();

        // Si aucun, retourner le premier compte courant
        if (comptePrincipal == null) {
            comptePrincipal = Compte.find(
                    "user = ?1 and type = ?2 and actif = true order by createdAt",
                    user,
                    TypeCompte.COMPTE_COURANT
            ).firstResult();
        }

        return comptePrincipal;
    }

    /**
     * D�marque tous les comptes d'un utilisateur comme non-principal,
     * sauf celui sp�cifi�.
     *
     * @param user L'utilisateur
     * @param excepteCompteId L'ID du compte � garder comme principal (peut �tre null)
     */
    public void unsetAllPrincipalChargesFixes(User user) {
        Compte.update("principalChargesFixes = false where user = ?1", user);
    }

    /**
     * Calcule l'argent libre sur un compte.
     * Argent libre = Solde total - Montant alloue aux objectifs
     *
     * @param compte Le compte
     * @return Le montant d'argent libre
     */
    public BigDecimal calculerArgentLibre(Compte compte) {
        // Somme de l'argent alloue a des objectifs depuis ce compte
        BigDecimal montantAlloue = ObjectifRepartition.find(
                "select coalesce(sum(r.montantActuel), 0) from ObjectifRepartition r " +
                "where r.compte = ?1 and r.objectif.actif = true",
                compte
        ).project(BigDecimal.class).firstResult();

        if (montantAlloue == null) {
            montantAlloue = BigDecimal.ZERO;
        }

        // Argent libre = Solde total - Montant alloue
        return compte.getSoldeTotal().subtract(montantAlloue);
    }
}
