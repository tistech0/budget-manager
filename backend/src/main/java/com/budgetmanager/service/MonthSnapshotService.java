package com.budgetmanager.service;

import com.budgetmanager.entity.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service pour gérer les snapshots mensuels.
 */
@ApplicationScoped
public class MonthSnapshotService {

    private static final Logger LOGGER = Logger.getLogger(MonthSnapshotService.class);

    // Fixed charge types
    private static final Set<TypeTransaction> FIXED_CHARGE_TYPES = Set.of(
            TypeTransaction.LOYER,
            TypeTransaction.ASSURANCE,
            TypeTransaction.ABONNEMENT,
            TypeTransaction.CREDIT_IMMOBILIER,
            TypeTransaction.CREDIT_CONSO,
            TypeTransaction.IMPOTS,
            TypeTransaction.MUTUELLE,
            TypeTransaction.FRAIS_BANCAIRE
    );

    // Variable expense types
    private static final Set<TypeTransaction> VARIABLE_EXPENSE_TYPES = Set.of(
            TypeTransaction.ALIMENTATION,
            TypeTransaction.RESTAURANT,
            TypeTransaction.TRANSPORT,
            TypeTransaction.ESSENCE,
            TypeTransaction.SHOPPING,
            TypeTransaction.LOISIRS,
            TypeTransaction.SANTE,
            TypeTransaction.BEAUTE,
            TypeTransaction.MAISON,
            TypeTransaction.EDUCATION,
            TypeTransaction.VOYAGE
    );

    // Income types
    private static final Set<TypeTransaction> INCOME_TYPES = Set.of(
            TypeTransaction.SALAIRE,
            TypeTransaction.PRIME,
            TypeTransaction.FREELANCE,
            TypeTransaction.ALLOCATION,
            TypeTransaction.REMBOURSEMENT
    );

    @Inject
    TransactionService transactionService;

    /**
     * Creates or updates a snapshot for a given budget cycle.
     *
     * @param user The user
     * @param month The month in YYYY-MM format (represents the cycle starting this month)
     * @return The created/updated snapshot
     */
    public MonthSnapshot createOrUpdateSnapshot(User user, String month) {
        int jourPaie = user.getJourPaie();

        // Calculate budget cycle dates
        String[] parts = month.split("-");
        int year = Integer.parseInt(parts[0]);
        int monthNum = Integer.parseInt(parts[1]);

        LocalDate cycleStart = LocalDate.of(year, monthNum, Math.min(jourPaie, LocalDate.of(year, monthNum, 1).lengthOfMonth()));
        LocalDate cycleEnd = cycleStart.plusMonths(1).withDayOfMonth(Math.min(jourPaie - 1, cycleStart.plusMonths(1).lengthOfMonth()));

        LOGGER.infof("Creating snapshot for user %s, month %s, cycle %s to %s",
                user.getId(), month, cycleStart, cycleEnd);

        // Find or create snapshot
        MonthSnapshot snapshot = MonthSnapshot.find("user = ?1 and month = ?2", user, month)
                .<MonthSnapshot>firstResultOptional()
                .orElseGet(() -> {
                    MonthSnapshot newSnapshot = new MonthSnapshot();
                    newSnapshot.setUser(user);
                    newSnapshot.setMonth(month);
                    return newSnapshot;
                });

        snapshot.setCycleStart(cycleStart.toString());
        snapshot.setCycleEnd(cycleEnd.toString());

        // Get all transactions in the budget cycle
        List<Transaction> transactions = Transaction.find(
                "user = ?1 and dateTransaction >= ?2 and dateTransaction <= ?3",
                user, cycleStart, cycleEnd
        ).list();

        // Calculate totals
        BigDecimal totalRevenus = BigDecimal.ZERO;
        BigDecimal totalChargesFixes = BigDecimal.ZERO;
        BigDecimal totalDepensesVariables = BigDecimal.ZERO;
        BigDecimal totalEpargne = BigDecimal.ZERO;
        int nombreChargesFixes = 0;
        int nombreDepensesVariables = 0;

        for (Transaction t : transactions) {
            if (INCOME_TYPES.contains(t.getType()) && t.getMontant().compareTo(BigDecimal.ZERO) > 0) {
                totalRevenus = totalRevenus.add(t.getMontant());
            } else if (FIXED_CHARGE_TYPES.contains(t.getType()) && t.getMontant().compareTo(BigDecimal.ZERO) < 0) {
                totalChargesFixes = totalChargesFixes.add(t.getMontant().abs());
                nombreChargesFixes++;
            } else if (VARIABLE_EXPENSE_TYPES.contains(t.getType()) && t.getMontant().compareTo(BigDecimal.ZERO) < 0) {
                totalDepensesVariables = totalDepensesVariables.add(t.getMontant().abs());
                nombreDepensesVariables++;
            } else if ((t.getType() == TypeTransaction.EPARGNE || t.getType() == TypeTransaction.INVESTISSEMENT)
                    && t.getMontant().compareTo(BigDecimal.ZERO) < 0) {
                totalEpargne = totalEpargne.add(t.getMontant().abs());
            }
        }

        // Calculate current compte courant balance
        BigDecimal soldeCompteCourant = Compte.find("user = ?1 and type = ?2 and actif = true", user, TypeCompte.COMPTE_COURANT)
                .<Compte>stream()
                .map(Compte::getSoldeTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Set snapshot values
        snapshot.setTotalRevenus(totalRevenus);
        snapshot.setTotalChargesFixes(totalChargesFixes);
        snapshot.setTotalDepensesVariables(totalDepensesVariables);
        snapshot.setTotalEpargne(totalEpargne);
        snapshot.setSoldeCompteCourant(soldeCompteCourant);
        snapshot.setNombreTransactions(transactions.size());
        snapshot.setNombreChargesFixes(nombreChargesFixes);
        snapshot.setNombreDepensesVariables(nombreDepensesVariables);

        // Store budget info
        BigDecimal salaire = user.getSalaireMensuelNet() != null ? user.getSalaireMensuelNet() : BigDecimal.ZERO;
        snapshot.setSalaireMensuel(salaire);
        snapshot.setBudgetChargesFixes(salaire.multiply(BigDecimal.valueOf(user.getPourcentageChargesFixes() / 100.0)));
        snapshot.setBudgetDepensesVariables(salaire.multiply(BigDecimal.valueOf(user.getPourcentageDepensesVariables() / 100.0)));

        snapshot.persist();

        LOGGER.infof("Snapshot created: revenus=%s, charges=%s, depenses=%s, epargne=%s",
                totalRevenus, totalChargesFixes, totalDepensesVariables, totalEpargne);

        return snapshot;
    }

    /**
     * Gets a snapshot for a given user and month.
     */
    public Optional<MonthSnapshot> getSnapshot(User user, String month) {
        return MonthSnapshot.find("user = ?1 and month = ?2", user, month).firstResultOptional();
    }

    /**
     * Gets all snapshots for a user, ordered by month descending.
     */
    public List<MonthSnapshot> getAllSnapshots(User user) {
        return MonthSnapshot.find("user = ?1 order by month desc", user).list();
    }

    /**
     * Calculates the previous month in YYYY-MM format.
     */
    public String getPreviousMonth(String currentMonth) {
        String[] parts = currentMonth.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);

        month--;
        if (month < 1) {
            month = 12;
            year--;
        }

        return String.format("%d-%02d", year, month);
    }
}
