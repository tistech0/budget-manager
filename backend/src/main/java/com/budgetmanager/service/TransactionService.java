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
import java.time.YearMonth;
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
     * Le cycle budgétaire va de (dateValidation - 1) à (dateValidation + 1 mois - 2)
     * Exemple: salaire le 26 oct -> cycle du 25 oct au 24 nov
     */
    private void processChargesFixes(User user, String mois, LocalDate dateValidation) {
        // Calculate budget cycle boundaries based on salary validation date
        // Cycle runs from (salaryDate - 1) to (salaryDate + 1 month - 2)
        // Example: salary on Oct 26 -> cycle from Oct 25 to Nov 24
        LocalDate cycleStart = dateValidation.minusDays(1);
        LocalDate cycleEnd = dateValidation.plusMonths(1).minusDays(2);

        LOGGER.infof("Processing charges fixes for budget cycle %s to %s (salary validated on %s)",
                cycleStart, cycleEnd, dateValidation);

        // Find all active charges that are valid during this cycle period
        // A charge is valid if:
        // - It started on or before the cycle ends (dateDebut <= cycleEnd)
        // - It hasn't ended yet OR it ends on or after the cycle starts (dateFin is null OR dateFin >= cycleStart)
        List<ChargeFixe> chargesFixes = ChargeFixe.find(
                "user = ?1 and actif = true " +
                "and dateDebut <= ?2 " +
                "and (dateFin is null or dateFin >= ?3)",
                user, cycleEnd, cycleStart
        ).list();

        LOGGER.infof("Found %d active charges fixes for this cycle", chargesFixes.size());

        for (ChargeFixe charge : chargesFixes) {
            // Calculate the actual transaction date based on jourPrelevement
            // The charge should fall within the budget cycle
            LocalDate chargeDate = calculateChargeDateInCycle(cycleStart, cycleEnd, charge.getJourPrelevement(), mois);

            // Only process if the charge date falls within the cycle
            if (chargeDate != null) {
                // Check if a transaction already exists for this charge in this cycle
                long existingCount = Transaction.count(
                        "user = ?1 and type = ?2 and description like ?3 and dateTransaction >= ?4 and dateTransaction <= ?5",
                        user,
                        charge.getCategorie(),
                        "%" + charge.getNom() + "%",
                        cycleStart,
                        cycleEnd
                );

                if (existingCount == 0) {
                    // Create the transaction for the recurring charge
                    Transaction chargeTransaction = new Transaction();
                    chargeTransaction.setUser(user);
                    chargeTransaction.setCompte(charge.getCompte());
                    chargeTransaction.setMontant(charge.getMontant().negate()); // Negative amount
                    chargeTransaction.setDescription(charge.getNom() + " - " + mois);
                    chargeTransaction.setType(charge.getCategorie());
                    chargeTransaction.setDateTransaction(chargeDate);
                    chargeTransaction.persist();

                    // Update account balance
                    charge.getCompte().setSoldeTotal(charge.getCompte().getSoldeTotal().add(chargeTransaction.getMontant()));

                    LOGGER.infof("Created charge fixe transaction: %s for %s on %s",
                            charge.getNom(), chargeTransaction.getMontant(), chargeDate);
                } else {
                    LOGGER.infof("Charge fixe %s already exists in this cycle, skipping", charge.getNom());
                }
            } else {
                LOGGER.infof("Charge fixe %s (day %d) does not fall within cycle %s to %s, skipping",
                        charge.getNom(), charge.getJourPrelevement(), cycleStart, cycleEnd);
            }
        }
    }

    /**
     * Calculate the date when a recurring charge should be applied within a budget cycle.
     * Tries the charge day in the given month first, then the next month if needed.
     *
     * @param cycleStart Start of the budget cycle
     * @param cycleEnd End of the budget cycle
     * @param jourPrelevement Day of month when the charge is applied (1-31)
     * @param mois Month string in format "YYYY-MM"
     * @return The charge date if it falls within the cycle, null otherwise
     */
    private LocalDate calculateChargeDateInCycle(LocalDate cycleStart, LocalDate cycleEnd, int jourPrelevement, String mois) {
        // Parse the month string (e.g., "2025-10")
        YearMonth yearMonth = YearMonth.parse(mois);

        // Try the charge day in the given month
        LocalDate chargeDate = yearMonth.atDay(Math.min(jourPrelevement, yearMonth.lengthOfMonth()));

        // If it falls within the cycle, use it
        if (!chargeDate.isBefore(cycleStart) && !chargeDate.isAfter(cycleEnd)) {
            return chargeDate;
        }

        // Otherwise, try the next month
        YearMonth nextMonth = yearMonth.plusMonths(1);
        chargeDate = nextMonth.atDay(Math.min(jourPrelevement, nextMonth.lengthOfMonth()));

        if (!chargeDate.isBefore(cycleStart) && !chargeDate.isAfter(cycleEnd)) {
            return chargeDate;
        }

        // Charge doesn't fall in this cycle
        return null;
    }

    /**
     * Vérifie et traite les charges fixes dues pour le cycle budgétaire en cours.
     * Le cycle va du jour de paie au jour de paie suivant - 1.
     * Appelé au chargement du dashboard pour s'assurer que les charges sont traitées.
     *
     * @param user Utilisateur
     * @return Liste des transactions créées
     */
    public List<Transaction> checkAndProcessDueCharges(User user) {
        LocalDate today = LocalDate.now();
        int jourPaie = user.getJourPaie();

        // Calculate budget cycle boundaries based on user's pay day
        // Cycle runs from jourPaie to jourPaie - 1 of next month
        LocalDate cycleStart;
        LocalDate cycleEnd;

        if (today.getDayOfMonth() >= jourPaie) {
            // We're after pay day this month - cycle is this month's pay day to next month's pay day - 1
            cycleStart = today.withDayOfMonth(Math.min(jourPaie, today.lengthOfMonth()));
            cycleEnd = today.plusMonths(1).withDayOfMonth(Math.min(jourPaie - 1, today.plusMonths(1).lengthOfMonth()));
        } else {
            // We're before pay day this month - cycle is last month's pay day to this month's pay day - 1
            cycleStart = today.minusMonths(1).withDayOfMonth(Math.min(jourPaie, today.minusMonths(1).lengthOfMonth()));
            cycleEnd = today.withDayOfMonth(Math.min(jourPaie - 1, today.lengthOfMonth()));
        }

        String cycleLabel = cycleStart.getYear() + "-" + String.format("%02d", cycleStart.getMonthValue());

        LOGGER.infof("Checking due charges for user %s - Budget cycle: %s to %s (today: %s)",
                user.getId(), cycleStart, cycleEnd, today);

        // Find all active charges
        List<ChargeFixe> chargesFixes = ChargeFixe.find(
                "user = ?1 and actif = true " +
                "and dateDebut <= ?2 " +
                "and (dateFin is null or dateFin >= ?3)",
                user, cycleEnd, cycleStart
        ).list();

        LOGGER.infof("Found %d active charges fixes", chargesFixes.size());

        List<Transaction> createdTransactions = new ArrayList<>();

        for (ChargeFixe charge : chargesFixes) {
            // Calculate the charge date within the current budget cycle
            LocalDate chargeDate = calculateChargeDateInBudgetCycle(cycleStart, cycleEnd, charge.getJourPrelevement());

            if (chargeDate == null) {
                LOGGER.infof("Charge %s (day %d) does not fall within cycle %s to %s, skipping",
                        charge.getNom(), charge.getJourPrelevement(), cycleStart, cycleEnd);
                continue;
            }

            // Check if the charge date has passed (or is today)
            if (!chargeDate.isAfter(today)) {
                // Check frequency to determine if charge should be processed this cycle
                if (!shouldProcessChargeThisCycle(charge, cycleStart)) {
                    LOGGER.infof("Charge %s skipped due to frequency %s",
                            charge.getNom(), charge.getFrequence());
                    continue;
                }

                // Check if a transaction already exists for this charge in this budget cycle
                long existingCount = Transaction.count(
                        "user = ?1 and type = ?2 and description like ?3 " +
                        "and dateTransaction >= ?4 and dateTransaction <= ?5",
                        user,
                        charge.getCategorie(),
                        "%" + charge.getNom() + "%",
                        cycleStart,
                        cycleEnd
                );

                if (existingCount == 0) {
                    // Create the transaction for the recurring charge
                    Transaction chargeTransaction = new Transaction();
                    chargeTransaction.setUser(user);
                    chargeTransaction.setCompte(charge.getCompte());
                    chargeTransaction.setMontant(charge.getMontant().negate()); // Negative amount
                    chargeTransaction.setDescription(charge.getNom() + " - " + cycleLabel);
                    chargeTransaction.setType(charge.getCategorie());
                    chargeTransaction.setDateTransaction(chargeDate);
                    chargeTransaction.persist();

                    // Update account balance
                    charge.getCompte().setSoldeTotal(
                            charge.getCompte().getSoldeTotal().add(chargeTransaction.getMontant())
                    );

                    // Initialize lazy relations
                    initializeTransactionRelations(chargeTransaction);

                    createdTransactions.add(chargeTransaction);

                    LOGGER.infof("Created charge fixe transaction: %s for %s on %s",
                            charge.getNom(), chargeTransaction.getMontant(), chargeDate);
                } else {
                    LOGGER.infof("Charge fixe %s already processed this cycle, skipping",
                            charge.getNom());
                }
            } else {
                LOGGER.infof("Charge %s (day %d, date %s) not yet due (today is %s)",
                        charge.getNom(), charge.getJourPrelevement(), chargeDate, today);
            }
        }

        LOGGER.infof("Processed %d new charge transactions", createdTransactions.size());
        return createdTransactions;
    }

    /**
     * Calculate the date when a charge should be applied within a budget cycle.
     *
     * @param cycleStart Start of the budget cycle
     * @param cycleEnd End of the budget cycle
     * @param jourPrelevement Day of month when the charge is applied (1-31)
     * @return The charge date if it falls within the cycle, null otherwise
     */
    private LocalDate calculateChargeDateInBudgetCycle(LocalDate cycleStart, LocalDate cycleEnd, int jourPrelevement) {
        // Try the charge day in the cycle start month
        LocalDate chargeDate = cycleStart.withDayOfMonth(
                Math.min(jourPrelevement, cycleStart.lengthOfMonth())
        );

        // If it falls within the cycle, use it
        if (!chargeDate.isBefore(cycleStart) && !chargeDate.isAfter(cycleEnd)) {
            return chargeDate;
        }

        // Otherwise, try the next month
        LocalDate nextMonth = cycleStart.plusMonths(1);
        chargeDate = nextMonth.withDayOfMonth(
                Math.min(jourPrelevement, nextMonth.lengthOfMonth())
        );

        if (!chargeDate.isBefore(cycleStart) && !chargeDate.isAfter(cycleEnd)) {
            return chargeDate;
        }

        // Charge doesn't fall in this cycle
        return null;
    }

    /**
     * Détermine si une charge doit être traitée ce cycle selon sa fréquence.
     */
    private boolean shouldProcessChargeThisCycle(ChargeFixe charge, LocalDate cycleStart) {
        LocalDate dateDebut = charge.getDateDebut();
        int monthsSinceStart = (cycleStart.getYear() - dateDebut.getYear()) * 12
                + (cycleStart.getMonthValue() - dateDebut.getMonthValue());

        return switch (charge.getFrequence()) {
            case MENSUELLE -> true;
            case BIMESTRIELLE -> monthsSinceStart % 2 == 0;
            case TRIMESTRIELLE -> monthsSinceStart % 3 == 0;
            case SEMESTRIELLE -> monthsSinceStart % 6 == 0;
            case ANNUELLE -> monthsSinceStart % 12 == 0;
        };
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
