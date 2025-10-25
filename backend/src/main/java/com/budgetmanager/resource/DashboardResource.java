package com.budgetmanager.resource;

import com.budgetmanager.dto.CompteResponse;
import com.budgetmanager.dto.DashboardResponse;
import com.budgetmanager.dto.ObjectifResponse;
import com.budgetmanager.dto.UserResponse;
import com.budgetmanager.entity.*;
import com.budgetmanager.service.CompteService;
import com.budgetmanager.service.ObjectifService;
import com.budgetmanager.service.SalaireValideService;
import com.budgetmanager.service.UserContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/api/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DashboardResource {

    private static final Logger LOG = Logger.getLogger(DashboardResource.class);

    @Inject
    UserContext userContext;

    @Inject
    ObjectifService objectifService;

    @Inject
    CompteService compteService;

    @Inject
    SalaireValideService salaireValideService;

    @GET
    @Path("/test")
    public Response test() {
        return Response.ok()
                .entity(Map.of(
                        "message", "🚀 Backend connecté !",
                        "timestamp", LocalDateTime.now().toString(),
                        "version", "2.0.0",
                        "status", "OK"
                ))
                .build();
    }

    @GET
    @Path("/{mois}")
    public Response getDashboard(@PathParam("mois") String mois) {
        User user = userContext.getCurrentUser();

        List<Compte> comptes = Compte.find("user = ?1 and actif = true", user).list();

        // Fetch objectifs with repartitions in a single query (avoids N+1)
        List<Objectif> objectifs = objectifService.findObjectifsWithRepartitions(user);

        // Convert entities to DTOs
        UserResponse userResponse = UserResponse.fromEntity(user);

        // Calculer l'argent libre pour chaque compte
        List<CompteResponse> compteResponses = comptes.stream()
                .map(compte -> {
                    BigDecimal argentLibre = compteService.calculerArgentLibre(compte);
                    return CompteResponse.fromEntityWithArgentLibre(compte, argentLibre);
                })
                .collect(Collectors.toList());
        List<ObjectifResponse> objectifResponses = objectifs.stream()
                .map(ObjectifResponse::fromEntity)
                .collect(Collectors.toList());

        // Vérifier si le salaire a été validé pour ce mois
        boolean salaireValide = salaireValideService.existsForMois(user, mois);

        DashboardResponse dashboard = DashboardResponse.builder()
                .mois(mois)
                .user(userResponse)
                .comptes(compteResponses)
                .objectifs(objectifResponses)
                .salaireValide(salaireValide)
                .timestamp(LocalDateTime.now())
                .build();

        return Response.ok(dashboard).build();
    }

    /**
     * DELETE /api/dashboard/month/{month}
     * Supprime toutes les données d'un mois (salaire validé, transactions, etc.)
     *
     * @param mois Le mois au format YYYY-MM
     * @return 204 No Content si succès, 400 si erreur, 404 si pas de données
     */
    @DELETE
    @Path("/month/{month}")
    @Transactional
    public Response deleteMonth(@PathParam("month") String mois) {
        try {
            User user = userContext.getCurrentUser();

            // Validation du format du mois
            if (mois == null || !mois.matches("\\d{4}-\\d{2}")) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Format de mois invalide. Utilisez YYYY-MM"))
                        .build();
            }

            // Parse the target month
            YearMonth targetYearMonth = YearMonth.parse(mois);

            // Vérifier que le salaire existe pour ce mois
            boolean salaireExists = salaireValideService.existsForMois(user, mois);
            if (!salaireExists) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Aucun salaire validé trouvé pour ce mois"))
                        .build();
            }

            LOG.infof("🗑️ Début de la suppression du mois %s pour l'utilisateur %s", mois, user.getId());

            // Calculer les dates de début et fin du mois
            LocalDate startDate = targetYearMonth.atDay(1);
            LocalDate endDate = targetYearMonth.atEndOfMonth();

            // 1. Récupérer toutes les transactions du mois
            List<Transaction> transactions = Transaction.find(
                    "user = ?1 and dateTransaction >= ?2 and dateTransaction <= ?3",
                    user, startDate, endDate
            ).list();

            LOG.infof("📝 %d transaction(s) trouvée(s) pour le mois %s", transactions.size(), mois);

            // 2. Pour chaque transaction, inverser les modifications sur les comptes et objectifs
            for (Transaction transaction : transactions) {
                // Ajuster le solde du compte (inverser l'opération)
                Compte compte = transaction.getCompte();
                if (compte != null) {
                    compte.setSoldeTotal(compte.getSoldeTotal().subtract(transaction.getMontant()));
                    compte.persist();
                    LOG.debugf("   ↩️ Solde du compte %s ajusté: %s", compte.getNom(), compte.getSoldeTotal());
                }

                // Ajuster les répartitions d'objectifs si la transaction est liée à un objectif
                if (transaction.getObjectif() != null) {
                    // Trouver la répartition correspondante pour ce compte et cet objectif
                    ObjectifRepartition repartition = ObjectifRepartition.find(
                            "objectif = ?1 and compte = ?2",
                            transaction.getObjectif(), transaction.getCompte()
                    ).firstResult();

                    if (repartition != null) {
                        // Soustraire le montant de la répartition
                        repartition.setMontantActuel(repartition.getMontantActuel().subtract(transaction.getMontant().abs()));

                        // Si le montant devient négatif ou zéro, le mettre à zéro
                        if (repartition.getMontantActuel().compareTo(BigDecimal.ZERO) <= 0) {
                            repartition.setMontantActuel(BigDecimal.ZERO);
                        }

                        repartition.persist();
                        LOG.debugf("   📊 Répartition de l'objectif %s ajustée: %s",
                                transaction.getObjectif().getNom(), repartition.getMontantActuel());
                    }
                }

                // Supprimer la transaction
                transaction.delete();
            }

            // 3. Supprimer le salaire validé pour ce mois
            boolean deleted = salaireValideService.deleteByMois(user, mois);

            if (deleted) {
                LOG.infof("✅ Mois %s supprimé avec succès (%d transaction(s) supprimée(s))",
                        mois, transactions.size());
                return Response.noContent().build();
            } else {
                LOG.warnf("⚠️ Échec de la suppression du salaire validé pour le mois %s", mois);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(Map.of("error", "Erreur lors de la suppression du salaire"))
                        .build();
            }

        } catch (Exception e) {
            LOG.errorf(e, "❌ Erreur lors de la suppression du mois %s", mois);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of(
                            "error", "Erreur interne du serveur",
                            "message", e.getMessage()
                    ))
                    .build();
        }
    }
}