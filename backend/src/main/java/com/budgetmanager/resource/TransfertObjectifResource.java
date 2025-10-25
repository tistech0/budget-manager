package com.budgetmanager.resource;

import com.budgetmanager.dto.*;
import com.budgetmanager.entity.*;
import com.budgetmanager.entity.TypeTransaction;
import com.budgetmanager.service.UserContext;
import com.budgetmanager.util.LazyLoadingUtil;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/api/transferts/objectifs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransfertObjectifResource {

    @Inject
    UserContext userContext;

    /**
     * GET /api/transferts/objectifs
     * Récupérer l'historique des transferts entre objectifs
     */
    @GET
    public Response getAllTransferts(
            @QueryParam("dateDebut") String dateDebut,
            @QueryParam("dateFin") String dateFin,
            @QueryParam("limit") @DefaultValue("50") Integer limit
    ) {
        User user = userContext.getCurrentUser();

        StringBuilder query = new StringBuilder("user = ?1");

        if (dateDebut != null) {
            query.append(" and dateTransfert >= ?2");
        }
        if (dateFin != null) {
            query.append(dateDebut != null ? " and dateTransfert <= ?3" : " and dateTransfert <= ?2");
        }

        query.append(" order by dateTransfert desc, createdAt desc");

        List<TransfertObjectif> transferts;
        if (dateDebut != null && dateFin != null) {
            transferts = TransfertObjectif.find(query.toString(), user,
                            LocalDate.parse(dateDebut), LocalDate.parse(dateFin))
                    .page(0, limit)
                    .list();
        } else if (dateDebut != null) {
            transferts = TransfertObjectif.find(query.toString(), user, LocalDate.parse(dateDebut))
                    .page(0, limit)
                    .list();
        } else {
            transferts = TransfertObjectif.find(query.toString(), user)
                    .page(0, limit)
                    .list();
        }

        // Charger toutes les relations
        transferts.forEach(LazyLoadingUtil::initializeTransfertObjectif);

        List<TransfertObjectifResponse> transfertResponses = transferts.stream()
                .map(TransfertObjectifResponse::fromEntity)
                .collect(Collectors.toList());

        return Response.ok(transfertResponses).build();
    }

    /**
     * GET /api/transferts/objectifs/{id}
     * Récupérer un transfert par ID
     */
    @GET
    @Path("/{id}")
    public Response getTransfert(@PathParam("id") UUID id) {
        TransfertObjectif transfert = TransfertObjectif.findById(id);
        if (transfert == null) {
            return Response.status(404)
                    .entity(new ErrorResponse("Transfert non trouvé"))
                    .build();
        }

        // Charger toutes les relations
        LazyLoadingUtil.initializeTransfertObjectif(transfert);

        return Response.ok(TransfertObjectifResponse.fromEntity(transfert)).build();
    }

    /**
     * POST /api/transferts/objectifs
     * Créer un nouveau transfert entre objectifs
     * Gère automatiquement les transactions et répartitions
     */
    @POST
    @Transactional
    public Response createTransfert(@Valid CreateTransfertObjectifRequest request) {
        User user = userContext.getCurrentUser();

        // Validation des objectifs
        Objectif objectifSource = Objectif.findById(request.getObjectifSourceId());
        if (objectifSource == null || !objectifSource.getActif()) {
            return Response.status(404)
                    .entity(new ErrorResponse("Objectif source non trouvé"))
                    .build();
        }

        Objectif objectifDestination = Objectif.findById(request.getObjectifDestinationId());
        if (objectifDestination == null || !objectifDestination.getActif()) {
            return Response.status(404)
                    .entity(new ErrorResponse("Objectif destination non trouvé"))
                    .build();
        }

        // Validation des comptes
        Compte compteSource = Compte.findById(request.getCompteSourceId());
        if (compteSource == null || !compteSource.getActif()) {
            return Response.status(404)
                    .entity(new ErrorResponse("Compte source non trouvé"))
                    .build();
        }

        Compte compteDestination = Compte.findById(request.getCompteDestinationId());
        if (compteDestination == null || !compteDestination.getActif()) {
            return Response.status(404)
                    .entity(new ErrorResponse("Compte destination non trouvé"))
                    .build();
        }

        // Validation du montant
        if (request.getMontant().compareTo(BigDecimal.ZERO) <= 0) {
            return Response.status(400)
                    .entity(new ErrorResponse("Le montant doit être positif"))
                    .build();
        }

        // Vérifier qu'il y a assez d'argent dans la répartition source
        ObjectifRepartition repartitionSource = ObjectifRepartition.find(
                "objectif = ?1 and compte = ?2",
                objectifSource, compteSource
        ).firstResult();

        if (repartitionSource == null) {
            return Response.status(400)
                    .entity(new ErrorResponse("Aucune répartition n'existe pour cet objectif sur ce compte"))
                    .build();
        }

        if (repartitionSource.getMontantActuel().compareTo(request.getMontant()) < 0) {
            return Response.status(400)
                    .entity(new ErrorResponse("Montant insuffisant dans la répartition source"))
                    .build();
        }

        // Créer l'entrée de transfert
        TransfertObjectif transfert = new TransfertObjectif(
                user,
                objectifSource,
                objectifDestination,
                compteSource,
                compteDestination,
                request.getMontant(),
                request.getMotif()
        );
        transfert.persist();

        // 1. Débiter la répartition source
        repartitionSource.setMontantActuel(repartitionSource.getMontantActuel().subtract(request.getMontant()));
        if (repartitionSource.getMontantActuel().compareTo(BigDecimal.ZERO) <= 0) {
            repartitionSource.delete();
        }

        // 2. Créditer la répartition destination (créer si inexistante)
        ObjectifRepartition repartitionDestination = ObjectifRepartition.find(
                "objectif = ?1 and compte = ?2",
                objectifDestination, compteDestination
        ).firstResult();

        if (repartitionDestination == null) {
            repartitionDestination = new ObjectifRepartition(
                    objectifDestination,
                    compteDestination,
                    request.getMontant()
            );
            repartitionDestination.persist();
        } else {
            repartitionDestination.setMontantActuel(repartitionDestination.getMontantActuel().add(request.getMontant()));
        }

        // 3. Créer la transaction de débit
        Transaction transactionDebit = new Transaction(
                user,
                compteSource,
                objectifSource,
                request.getMontant().negate(),
                TypeTransaction.TRANSFERT_OBJECTIF,
                "Transfert vers " + objectifDestination.getNom() + " : " +
                        (request.getMotif() != null ? request.getMotif() : "")
        );
        transactionDebit.setTransfertObjectif(transfert);
        transactionDebit.persist();

        // 4. Créer la transaction de crédit
        Transaction transactionCredit = new Transaction(
                user,
                compteDestination,
                objectifDestination,
                request.getMontant(),
                TypeTransaction.TRANSFERT_OBJECTIF,
                "Transfert depuis " + objectifSource.getNom() + " : " +
                        (request.getMotif() != null ? request.getMotif() : "")
        );
        transactionCredit.setTransfertObjectif(transfert);
        transactionCredit.persist();

        // 5. Ajuster les soldes des comptes si différents
        if (!compteSource.getId().equals(compteDestination.getId())) {
            compteSource.setSoldeTotal(compteSource.getSoldeTotal().subtract(request.getMontant()));
            compteDestination.setSoldeTotal(compteDestination.getSoldeTotal().add(request.getMontant()));
        }

        // Charger les relations pour la réponse
        LazyLoadingUtil.initializeTransfertObjectif(transfert);

        return Response.status(201).entity(TransfertObjectifResponse.fromEntity(transfert)).build();
    }

    /**
     * DELETE /api/transferts/objectifs/{id}
     * Annuler un transfert (avec toutes ses conséquences)
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response annulerTransfert(@PathParam("id") UUID id) {
        TransfertObjectif transfert = TransfertObjectif.findById(id);
        if (transfert == null) {
            return Response.status(404)
                    .entity(new ErrorResponse("Transfert non trouvé"))
                    .build();
        }

        // 1. Restaurer la répartition source
        ObjectifRepartition repartitionSource = ObjectifRepartition.find(
                "objectif = ?1 and compte = ?2",
                transfert.getObjectifSource(), transfert.getCompteSource()
        ).firstResult();

        if (repartitionSource == null) {
            repartitionSource = new ObjectifRepartition(
                    transfert.getObjectifSource(),
                    transfert.getCompteSource(),
                    transfert.getMontant()
            );
            repartitionSource.persist();
        } else {
            repartitionSource.setMontantActuel(repartitionSource.getMontantActuel().add(transfert.getMontant()));
        }

        // 2. Débiter la répartition destination
        ObjectifRepartition repartitionDestination = ObjectifRepartition.find(
                "objectif = ?1 and compte = ?2",
                transfert.getObjectifDestination(), transfert.getCompteDestination()
        ).firstResult();

        if (repartitionDestination != null) {
            repartitionDestination.setMontantActuel(repartitionDestination.getMontantActuel().subtract(transfert.getMontant()));
            if (repartitionDestination.getMontantActuel().compareTo(BigDecimal.ZERO) <= 0) {
                repartitionDestination.delete();
            }
        }

        // 3. Restaurer les soldes des comptes
        if (!transfert.getCompteSource().getId().equals(transfert.getCompteDestination().getId())) {
            transfert.getCompteSource().setSoldeTotal(transfert.getCompteSource().getSoldeTotal().add(transfert.getMontant()));
            transfert.getCompteDestination().setSoldeTotal(transfert.getCompteDestination().getSoldeTotal().subtract(transfert.getMontant()));
        }

        // 4. Supprimer les transactions liées
        Transaction.delete("transfertObjectif = ?1", transfert);

        // 5. Supprimer le transfert
        transfert.delete();

        return Response.ok(new MessageResponse("Transfert annulé avec succès")).build();
    }
}