package com.budgetmanager.service;

import com.budgetmanager.entity.User;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.NotFoundException;

/**
 * Service pour gérer le contexte utilisateur dans la requête.
 * Pour l'instant, retourne le premier utilisateur (app mono-utilisateur).
 * À remplacer par une vraie authentification (JWT, OIDC) plus tard.
 */
@RequestScoped
public class UserContext {

    private User currentUser;

    /**
     * Récupère l'utilisateur courant.
     * Pour l'instant, retourne le premier utilisateur de la base.
     * TODO: Implémenter l'authentification JWT/OIDC
     *
     * @return L'utilisateur courant
     * @throws NotFoundException si aucun utilisateur n'existe
     */
    public User getCurrentUser() {
        if (currentUser == null) {
            currentUser = User.find("order by createdAt").firstResult();
            if (currentUser == null) {
                throw new NotFoundException("Aucun profil utilisateur trouvé");
            }
        }
        return currentUser;
    }

    /**
     * Vérifie si un utilisateur existe.
     *
     * @return true si un utilisateur existe, false sinon
     */
    public boolean hasUser() {
        try {
            getCurrentUser();
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }
}
