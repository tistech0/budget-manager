package com.budgetmanager.resource;

import com.budgetmanager.dto.*;
import com.budgetmanager.entity.*;
import com.budgetmanager.service.ObjectifService;
import com.budgetmanager.service.UserContext;
import com.budgetmanager.util.LazyLoadingUtil;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/api/objectifs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ObjectifResource {

    @Inject
    UserContext userContext;

    @Inject
    ObjectifService objectifService;

    // ===============================================
    // ENDPOINTS OBJECTIFS (existants)
    // ===============================================

    @GET
    public List<ObjectifResponse> getAllObjectifs(
            @QueryParam("page") @DefaultValue("0") Integer page,
            @QueryParam("limit") @DefaultValue("50") Integer limit) {
        User user = userContext.getCurrentUser();

        List<Objectif> objectifs = Objectif
                .find("user = ?1 and actif = true order by priorite, nom", user)
                .page(page, limit)
                .list();

        // Charger les répartitions et calculer montantActuel
        objectifs.forEach(objectifService::enrichirObjectif);

        return objectifs.stream()
                .map(ObjectifResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id}")
    public Response getObjectif(@PathParam("id") UUID id) {
        Objectif objectif = Objectif.findById(id);
        if (objectif == null || !objectif.getActif()) {
            return Response.status(404)
                    .entity(new ErrorResponse("Objectif non trouvé"))
                    .build();
        }

        objectifService.enrichirObjectif(objectif);
        return Response.ok(ObjectifResponse.fromEntity(objectif)).build();
    }

    @POST
    @Transactional
    public Response createObjectif(@Valid CreateObjectifRequest request) {
        User user = userContext.getCurrentUser();

        // Créer l'objectif
        Objectif objectif = new Objectif(user, request.getNom(), request.getMontantCible(), request.getPriorite(), request.getType());
        objectif.setCouleur(request.getCouleur());
        objectif.setIcone(request.getIcone());
        objectif.setDescription(request.getDescription());
        objectif.persist();

        // Créer les répartitions initiales si présentes
        if (request.getRepartitions() != null && !request.getRepartitions().isEmpty()) {
            int ordre = 1;
            for (CreateObjectifRequest.InitialRepartition repartitionRequest : request.getRepartitions()) {
                // Trouver le compte par ID
                UUID compteId;
                try {
                    compteId = UUID.fromString(repartitionRequest.getAccountId());
                } catch (IllegalArgumentException e) {
                    // Si accountId n'est pas un UUID valide, skip
                    continue;
                }

                Compte compte = Compte.findById(compteId);
                if (compte == null || !compte.getActif()) {
                    continue; // Skip si compte n'existe pas
                }

                // Créer la répartition
                ObjectifRepartition repartition = new ObjectifRepartition();
                repartition.setObjectif(objectif);
                repartition.setCompte(compte);
                repartition.setMontantActuel(repartitionRequest.getMontant() != null ? repartitionRequest.getMontant() : BigDecimal.ZERO);
                repartition.setOrdre(ordre++);
                repartition.persist();
            }
        }

        // Enrichir avec les répartitions créées
        objectifService.enrichirObjectif(objectif);
        return Response.status(201).entity(ObjectifResponse.fromEntity(objectif)).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateObjectif(@PathParam("id") UUID id, @Valid UpdateObjectifRequest request) {
        Objectif objectif = Objectif.findById(id);
        if (objectif == null || !objectif.getActif()) {
            return Response.status(404)
                    .entity(new ErrorResponse("Objectif non trouvé"))
                    .build();
        }

        // Mise à jour des champs
        if (request.getNom() != null) objectif.setNom(request.getNom());
        if (request.getMontantCible() != null) objectif.setMontantCible(request.getMontantCible());
        if (request.getPriorite() != null) objectif.setPriorite(request.getPriorite());
        if (request.getCouleur() != null) objectif.setCouleur(request.getCouleur());
        if (request.getIcone() != null) objectif.setIcone(request.getIcone());
        if (request.getDescription() != null) objectif.setDescription(request.getDescription());

        objectifService.enrichirObjectif(objectif);
        return Response.ok(ObjectifResponse.fromEntity(objectif)).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteObjectif(@PathParam("id") UUID id) {
        Objectif objectif = Objectif.findById(id);
        if (objectif == null) {
            return Response.status(404)
                    .entity(new ErrorResponse("Objectif non trouvé"))
                    .build();
        }

        // Soft delete
        objectif.setActif(false);
        return Response.ok(new MessageResponse("Objectif supprimé avec succès")).build();
    }

    // ===============================================
    // ENDPOINTS RÉPARTITIONS (nouveaux)
    // ===============================================

    /**
     * GET /api/objectifs/{id}/repartitions
     * Récupérer toutes les répartitions d'un objectif
     */
    @GET
    @Path("/{id}/repartitions")
    public Response getRepartitions(@PathParam("id") UUID objectifId) {
        Objectif objectif = Objectif.findById(objectifId);
        if (objectif == null || !objectif.getActif()) {
            return Response.status(404)
                    .entity(new ErrorResponse("Objectif non trouvé"))
                    .build();
        }

        List<ObjectifRepartition> repartitions = ObjectifRepartition.find(
                "objectif = ?1 order by ordre",
                objectif
        ).list();

        // Charger les comptes
        repartitions.forEach(LazyLoadingUtil::initializeObjectifRepartition);

        List<ObjectifRepartitionResponse> repartitionResponses = repartitions.stream()
                .map(ObjectifRepartitionResponse::fromEntity)
                .collect(Collectors.toList());

        return Response.ok(repartitionResponses).build();
    }

    /**
     * POST /api/objectifs/{id}/repartitions
     * Ajouter une nouvelle répartition à un objectif
     */
    @POST
    @Path("/{id}/repartitions")
    @Transactional
    public Response ajouterRepartition(
            @PathParam("id") UUID objectifId,
            @Valid CreateRepartitionRequest request
    ) {
        Objectif objectif = Objectif.findById(objectifId);
        if (objectif == null || !objectif.getActif()) {
            return Response.status(404)
                    .entity(new ErrorResponse("Objectif non trouvé"))
                    .build();
        }

        Compte compte = Compte.findById(request.getCompteId());
        if (compte == null || !compte.getActif()) {
            return Response.status(404)
                    .entity(new ErrorResponse("Compte non trouvé"))
                    .build();
        }

        // Vérifier qu'une répartition n'existe pas déjà pour ce couple objectif-compte
        long exists = ObjectifRepartition.count("objectif = ?1 and compte = ?2", objectif, compte);
        if (exists > 0) {
            return Response.status(400)
                    .entity(new ErrorResponse("Une répartition existe déjà pour cet objectif sur ce compte"))
                    .build();
        }

        // Créer la répartition
        ObjectifRepartition repartition = new ObjectifRepartition();
        repartition.setObjectif(objectif);
        repartition.setCompte(compte);
        repartition.setMontantActuel(request.getMontantActuel() != null ? request.getMontantActuel() : BigDecimal.ZERO);
        repartition.setPourcentageCible(request.getPourcentageCible());
        repartition.setOrdre(request.getOrdre() != null ? request.getOrdre() : 1);
        repartition.persist();

        // Charger le compte pour la réponse
        LazyLoadingUtil.initializeObjectifRepartition(repartition);

        return Response.status(201).entity(ObjectifRepartitionResponse.fromEntity(repartition)).build();
    }

    /**
     * PUT /api/objectifs/{objectifId}/repartitions/{repartitionId}
     * Mettre à jour une répartition
     */
    @PUT
    @Path("/{objectifId}/repartitions/{repartitionId}")
    @Transactional
    public Response updateRepartition(
            @PathParam("objectifId") UUID objectifId,
            @PathParam("repartitionId") UUID repartitionId,
            @Valid UpdateRepartitionRequest request
    ) {
        ObjectifRepartition repartition = ObjectifRepartition.findById(repartitionId);
        if (repartition == null) {
            return Response.status(404)
                    .entity(new ErrorResponse("Répartition non trouvée"))
                    .build();
        }

        if (!repartition.getObjectif().getId().equals(objectifId)) {
            return Response.status(400)
                    .entity(new ErrorResponse("Cette répartition n'appartient pas à cet objectif"))
                    .build();
        }

        // Mise à jour des champs
        if (request.getMontantActuel() != null) {
            repartition.setMontantActuel(request.getMontantActuel());
        }
        if (request.getPourcentageCible() != null) {
            repartition.setPourcentageCible(request.getPourcentageCible());
        }
        if (request.getOrdre() != null) {
            repartition.setOrdre(request.getOrdre());
        }

        LazyLoadingUtil.initializeObjectifRepartition(repartition);
        return Response.ok(ObjectifRepartitionResponse.fromEntity(repartition)).build();
    }

    /**
     * DELETE /api/objectifs/{objectifId}/repartitions/{repartitionId}
     * Supprimer une répartition
     */
    @DELETE
    @Path("/{objectifId}/repartitions/{repartitionId}")
    @Transactional
    public Response deleteRepartition(
            @PathParam("objectifId") UUID objectifId,
            @PathParam("repartitionId") UUID repartitionId
    ) {
        ObjectifRepartition repartition = ObjectifRepartition.findById(repartitionId);
        if (repartition == null) {
            return Response.status(404)
                    .entity(new ErrorResponse("Répartition non trouvée"))
                    .build();
        }

        if (!repartition.getObjectif().getId().equals(objectifId)) {
            return Response.status(400)
                    .entity(new ErrorResponse("Cette répartition n'appartient pas à cet objectif"))
                    .build();
        }

        repartition.delete();
        return Response.ok(new MessageResponse("Répartition supprimée avec succès")).build();
    }

}