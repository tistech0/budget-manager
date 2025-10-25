package com.budgetmanager.service;

import com.budgetmanager.entity.*;
import com.budgetmanager.util.DateUtil;
import com.budgetmanager.util.LazyLoadingUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.hibernate.Hibernate;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service pour gérer la logique métier des transactions.
 */
@ApplicationScoped
public class TransactionService {

    private static final Logger LOGGER = Logger.getLogger(TransactionService.class);

    @Inject
    SalaireValideService salaireValideService;

    /**
     * Récupère les transactions avec filtres optionnels.
     *
     * @param user Utilisateur
     * @param dateDebut Date de début (optionnel)
     * @param dateFin Date de fin (optionnel)
     * @param type Type de transaction (optionnel)
     * @param compteId ID du compte (optionnel)
     * @param objectifId ID de l'objectif (optionnel)
     * @param limit Limite de résultats
     * @return Liste de transactions
     */
    public List<Transaction> getTransactionsFiltered(
            User user,
            String dateDebut,
            String dateFin,
            TypeTransaction type,
            UUID compteId,
            UUID objectifId,
            Integer limit
    ) {
        // Construction de la requête dynamique
        StringBuilder query = new StringBuilder("user = ?1");
        int paramIndex = 2;

        if (dateDebut != null) {
            query.append(" and dateTransaction >= ?").append(paramIndex++);
        }
        if (dateFin != null) {
            query.append(" and dateTransaction <= ?").append(paramIndex++);
        }
        if (type != null) {
            query.append(" and type = ?").append(paramIndex++);
        }
        if (compteId != null) {
            query.append(" and compte.id = ?").append(paramIndex++);
        }
        if (objectifId != null) {
            query.append(" and objectif.id = ?").append(paramIndex++);
        }

        query.append(" order by dateTransaction desc, createdAt desc");

        // Construction des paramètres
        List<Object> params = new ArrayList<>();
        params.add(user);

        if (dateDebut != null) params.add(DateUtil.parseDate(dateDebut));
        if (dateFin != null) params.add(DateUtil.parseDate(dateFin));
        if (type != null) params.add(type);
        if (compteId != null) params.add(compteId);
        if (objectifId != null) params.add(objectifId);

        return Transaction.find(query.toString(), params.toArray())
                .page(0, limit)
                .list();
    }

    /**
     * Crée une nouvelle transaction.
     *
     * @param user Utilisateur
     * @param compteId ID du compte
     * @param objectifId ID de l'objectif (optionnel)
     * @param montant Montant
     * @param description Description
     * @param type Type de transaction
     * @param dateTransaction Date de transaction (optionnel)
     * @return Transaction créée
     */
    public Transaction createTransaction(
            User user,
            UUID compteId,
            UUID objectifId,
            BigDecimal montant,
            String description,
            TypeTransaction type,
            String dateTransaction
    ) {
        Compte compte = Compte.findById(compteId);
        if (compte == null || !compte.getActif()) {
            throw new NotFoundException("Compte non trouvé");
        }

        // Vérifier l'objectif si fourni
        Objectif objectif = null;
        if (objectifId != null) {
            objectif = Objectif.findById(objectifId);
            if (objectif == null || !objectif.getActif()) {
                throw new NotFoundException("Objectif non trouvé");
            }
        }

        // Créer la transaction
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setCompte(compte);
        transaction.setObjectif(objectif);
        transaction.setMontant(montant);
        transaction.setDescription(description);
        transaction.setType(type);
        transaction.setDateTransaction(DateUtil.parseDateOrNow(dateTransaction));
        transaction.persist();

        // Mettre à jour le solde du compte
        compte.setSoldeTotal(compte.getSoldeTotal().add(montant));

        // Si transaction liée à un objectif avec montant positif, mettre à jour la répartition
        if (objectif != null && montant.compareTo(BigDecimal.ZERO) > 0) {
            updateObjectifRepartition(objectif, compte, montant);
        }

        // Initialiser les relations lazy
        initializeTransactionRelations(transaction);

        return transaction;
    }

    /**
     * Met à jour une transaction existante.
     *
     * @param transactionId ID de la transaction
     * @param description Nouvelle description (optionnel)
     * @param montant Nouveau montant (optionnel)
     * @param dateTransaction Nouvelle date (optionnel)
     * @return Transaction mise à jour
     */
    public Transaction updateTransaction(
            UUID transactionId,
            String description,
            BigDecimal montant,
            String dateTransaction
    ) {
        Transaction transaction = Transaction.findById(transactionId);
        if (transaction == null) {
            throw new NotFoundException("Transaction non trouvée");
        }

        // Sauvegarder l'ancien montant pour ajuster le solde
        BigDecimal ancienMontant = transaction.getMontant();

        // Mise à jour des champs
        if (description != null) {
            transaction.setDescription(description);
        }

        if (montant != null) {
            // Ajuster le solde du compte
            BigDecimal difference = montant.subtract(ancienMontant);
            transaction.getCompte().setSoldeTotal(transaction.getCompte().getSoldeTotal().add(difference));

            // Ajuster la répartition si liée à un objectif
            if (transaction.getObjectif() != null) {
                ObjectifRepartition repartition = ObjectifRepartition.find(
                        "objectif = ?1 and compte = ?2",
                        transaction.getObjectif(), transaction.getCompte()
                ).firstResult();

                if (repartition != null) {
                    repartition.setMontantActuel(repartition.getMontantActuel().add(difference));
                }
            }

            transaction.setMontant(montant);
        }

        if (dateTransaction != null) {
            transaction.setDateTransaction(DateUtil.parseDate(dateTransaction));
        }

        // Initialiser les relations lazy
        initializeTransactionRelations(transaction);

        return transaction;
    }

    /**
     * Supprime une transaction.
     *
     * @param transactionId ID de la transaction
     */
    public void deleteTransaction(UUID transactionId) {
        Transaction transaction = Transaction.findById(transactionId);
        if (transaction == null) {
            throw new NotFoundException("Transaction non trouvée");
        }

        // Annuler l'impact sur le solde du compte
        transaction.getCompte().setSoldeTotal(transaction.getCompte().getSoldeTotal().subtract(transaction.getMontant()));

        // Si liée à un objectif, ajuster la répartition
        if (transaction.getObjectif() != null) {
            ObjectifRepartition repartition = ObjectifRepartition.find(
                    "objectif = ?1 and compte = ?2",
                    transaction.getObjectif(),
                    transaction.getCompte()
            ).firstResult();

            if (repartition != null) {
                repartition.setMontantActuel(repartition.getMontantActuel().subtract(transaction.getMontant()));
                if (repartition.getMontantActuel().compareTo(BigDecimal.ZERO) <= 0) {
                    repartition.delete();
                }
            }
        }

        transaction.delete();
    }

    /**
     * Valide un salaire et traite automatiquement les charges fixes.
     *
     * @param user Utilisateur
     * @param compteId ID du compte (optionnel)
     * @param mois Mois au format YYYY-MM
     * @param montant Montant (optionnel pour SALAIRE)
     * @param type Type de transaction (SALAIRE, PRIME, FREELANCE)
     * @param description Description (optionnel)
     * @param dateReception Date de réception (optionnel)
     * @return Transaction créée
     */
    public Transaction validerSalaire(
            User user,
            UUID compteId,
            String mois,
            BigDecimal montant,
            TypeTransaction type,
            String description,
            String dateReception
    ) {
        // Déterminer le type de revenu (défaut: SALAIRE)
        TypeTransaction typeRevenu = type != null ? type : TypeTransaction.SALAIRE;

        // Déterminer le compte
        Compte compte = determineCompteForSalaire(user, compteId);

        // Déterminer le montant (obligatoire pour PRIME/FREELANCE, optionnel pour SALAIRE)
        BigDecimal montantFinal = determineMontantForSalaire(user, montant, typeRevenu);

        // Générer la description si non fournie
        String descriptionFinal = generateSalaireDescription(description, typeRevenu, mois);

        // Créer la transaction
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setCompte(compte);
        transaction.setMontant(montantFinal);
        transaction.setDescription(descriptionFinal);
        transaction.setType(typeRevenu);
        transaction.setDateTransaction(DateUtil.parseDateOrNow(dateReception));
        transaction.persist();

        // Mettre à jour le solde
        compte.setSoldeTotal(compte.getSoldeTotal().add(transaction.getMontant()));

        // Si c'est un SALAIRE, créer ou mettre à jour le SalaireValide pour ce mois
        if (typeRevenu == TypeTransaction.SALAIRE) {
            salaireValideService.createOrUpdate(
                    user,
                    mois,
                    montantFinal,
                    transaction.getDateTransaction(),
                    compte,
                    descriptionFinal
            );

            // Traiter automatiquement les charges fixes
            processChargesFixes(user, mois, transaction.getDateTransaction());
        }

        // Initialiser les relations lazy
        initializeTransactionRelations(transaction);

        return transaction;
    }

    /**
     * Met à jour ou crée une répartition d'objectif.
     */
    private void updateObjectifRepartition(Objectif objectif, Compte compte, BigDecimal montant) {
        ObjectifRepartition repartition = ObjectifRepartition.find(
                "objectif = ?1 and compte = ?2", objectif, compte
        ).firstResult();

        if (repartition == null) {
            repartition = new ObjectifRepartition();
            repartition.setObjectif(objectif);
            repartition.setCompte(compte);
            repartition.setMontantActuel(montant);
            repartition.setOrdre(1);
            repartition.persist();
        } else {
            repartition.setMontantActuel(repartition.getMontantActuel().add(montant));
        }
    }

    /**
     * Initialise les relations lazy d'une transaction.
     */
    private void initializeTransactionRelations(Transaction transaction) {
        LazyLoadingUtil.initializeTransaction(transaction);
    }

    /**
     * Détermine le compte à utiliser pour un salaire.
     */
    private Compte determineCompteForSalaire(User user, UUID compteId) {
        if (compteId != null) {
            Compte compte = Compte.findById(compteId);
            if (compte == null || !compte.getActif()) {
                throw new NotFoundException("Compte non trouvé");
            }
            return compte;
        }

        // Chercher le compte principal charges fixes
        Compte compte = Compte.find("user = ?1 and principalChargesFixes = true and actif = true", user)
                .firstResult();

        // Sinon, prendre le premier compte courant
        if (compte == null) {
            compte = Compte.find("user = ?1 and type = 'COMPTE_COURANT' and actif = true", user)
                    .firstResult();
        }

        if (compte == null) {
            throw new NotFoundException("Aucun compte disponible");
        }

        return compte;
    }

    /**
     * Détermine le montant pour un salaire.
     */
    private BigDecimal determineMontantForSalaire(User user, BigDecimal montant, TypeTransaction type) {
        if (type == TypeTransaction.SALAIRE && montant == null) {
            return user.getSalaireMensuelNet();
        } else if (montant == null) {
            throw new IllegalArgumentException("Le montant est requis pour " + type);
        }
        return montant;
    }

    /**
     * Génère une description pour un salaire.
     */
    private String generateSalaireDescription(String description, TypeTransaction type, String mois) {
        if (description != null && !description.isBlank()) {
            return description;
        }

        String typeLabel = switch (type) {
            case SALAIRE -> "Salaire";
            case PRIME -> "Prime";
            case FREELANCE -> "Freelance";
            default -> "Revenu";
        };
        return typeLabel + " " + mois;
    }

    /**
     * Traite les charges fixes automatiquement après validation du salaire.
     */
    private void processChargesFixes(User user, String mois, LocalDate dateValidation) {
        int jourValidation = dateValidation.getDayOfMonth();
        List<ChargeFixe> chargesFixes = ChargeFixe.find(
                "user = ?1 and actif = true and jourPrelevement <= ?2",
                user, jourValidation
        ).list();

        LOGGER.infof("Processing %d charges fixes for validation day %d", chargesFixes.size(), jourValidation);

        LocalDate debutMois = DateUtil.getFirstDayOfMonth(mois);
        LocalDate finMois = DateUtil.getLastDayOfMonth(mois);

        for (ChargeFixe charge : chargesFixes) {
            // Vérifier si une transaction existe déjà pour cette charge ce mois-ci
            long existingCount = Transaction.count(
                    "user = ?1 and type = ?2 and description like ?3 and dateTransaction >= ?4 and dateTransaction <= ?5",
                    user,
                    charge.getCategorie(),
                    "%" + charge.getNom() + "%",
                    debutMois,
                    finMois
            );

            if (existingCount == 0) {
                // Créer la transaction pour la charge fixe
                Transaction chargeTransaction = new Transaction();
                chargeTransaction.setUser(user);
                chargeTransaction.setCompte(charge.getCompte());
                chargeTransaction.setMontant(charge.getMontant().negate()); // Montant négatif
                chargeTransaction.setDescription(charge.getNom() + " - " + mois);
                chargeTransaction.setType(charge.getCategorie());
                chargeTransaction.setDateTransaction(debutMois.withDayOfMonth(
                        Math.min(charge.getJourPrelevement(), debutMois.lengthOfMonth())
                ));
                chargeTransaction.persist();

                // Mettre à jour le solde du compte
                charge.getCompte().setSoldeTotal(charge.getCompte().getSoldeTotal().add(chargeTransaction.getMontant()));

                LOGGER.infof("Created charge fixe transaction: %s for %s", charge.getNom(), chargeTransaction.getMontant());
            }
        }
    }

    /**
     * Vérifie si un type de transaction est un revenu.
     *
     * @param type Le type de transaction
     * @return true si c'est un revenu, false sinon
     */
    public boolean isRevenu(TypeTransaction type) {
        return type == TypeTransaction.SALAIRE ||
                type == TypeTransaction.PRIME ||
                type == TypeTransaction.FREELANCE ||
                type == TypeTransaction.ALLOCATION ||
                type == TypeTransaction.REMBOURSEMENT ||
                type == TypeTransaction.GAIN_INVESTISSEMENT ||
                type == TypeTransaction.CADEAU_RECU ||
                type == TypeTransaction.VENTE;
    }

    /**
     * Vérifie si un type de transaction est une charge fixe.
     *
     * @param type Le type de transaction
     * @return true si c'est une charge fixe, false sinon
     */
    public boolean isChargeFixe(TypeTransaction type) {
        return type == TypeTransaction.LOYER ||
                type == TypeTransaction.ASSURANCE ||
                type == TypeTransaction.ABONNEMENT ||
                type == TypeTransaction.CREDIT_IMMOBILIER ||
                type == TypeTransaction.CREDIT_CONSO ||
                type == TypeTransaction.IMPOTS ||
                type == TypeTransaction.MUTUELLE;
    }

    /**
     * Vérifie si un type de transaction est une dépense variable.
     *
     * @param type Le type de transaction
     * @return true si c'est une dépense variable, false sinon
     */
    public boolean isDepenseVariable(TypeTransaction type) {
        return type == TypeTransaction.ALIMENTATION ||
                type == TypeTransaction.RESTAURANT ||
                type == TypeTransaction.TRANSPORT ||
                type == TypeTransaction.ESSENCE ||
                type == TypeTransaction.SHOPPING ||
                type == TypeTransaction.LOISIRS ||
                type == TypeTransaction.SANTE ||
                type == TypeTransaction.BEAUTE ||
                type == TypeTransaction.MAISON ||
                type == TypeTransaction.EDUCATION ||
                type == TypeTransaction.VOYAGE;
    }
}
