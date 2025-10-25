package com.budgetmanager.resource;

import com.budgetmanager.dto.CompteResponse;
import com.budgetmanager.dto.CreateCompteRequest;
import com.budgetmanager.dto.ErrorResponse;
import com.budgetmanager.dto.MessageResponse;
import com.budgetmanager.dto.UpdateCompteRequest;
import com.budgetmanager.entity.Banque;
import com.budgetmanager.entity.Compte;
import com.budgetmanager.entity.User;
import com.budgetmanager.service.CompteService;
import com.budgetmanager.service.UserContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/api/comptes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompteResource {

    @Inject
    UserContext userContext;

    @Inject
    CompteService compteService;

    @GET
    public List<CompteResponse> getAllComptes(
            @QueryParam("page") @DefaultValue("0") Integer page,
            @QueryParam("limit") @DefaultValue("50") Integer limit) {
        User user = userContext.getCurrentUser();
        List<Compte> comptes = compteService.getComptesActifs(user, page, limit);
        return comptes.stream()
                .map(CompteResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id}")
    public Response getCompte(@PathParam("id") UUID id) {
        Compte compte = Compte.findById(id);
        if (compte == null || !compte.getActif()) {
            return Response.status(404)
                    .entity(new ErrorResponse("Compte non trouvé"))
                    .build();
        }
        return Response.ok(CompteResponse.fromEntity(compte)).build();
    }

    @POST
    @Transactional
    public Response createCompte(@Valid CreateCompteRequest request) {
        User user = userContext.getCurrentUser();

        Banque banque = Banque.findById(request.getBanqueId());
        if (banque == null) {
            return Response.status(400)
                    .entity(new ErrorResponse("Banque non trouvée"))
                    .build();
        }

        Compte compte = new Compte(user, banque, request.getNom(), request.getType(), request.getSoldeTotal());
        compte.setTaux(request.getTaux());
        compte.setPlafond(request.getPlafond());
        compte.setDateOuverture(request.getDateOuverture());

        if (request.getPrincipalChargesFixes() != null && request.getPrincipalChargesFixes()) {
            // Démarquer tous les autres comptes du même utilisateur
            compteService.unsetAllPrincipalChargesFixes(user);
        }

        compte.setPrincipalChargesFixes(request.getPrincipalChargesFixes() != null ? request.getPrincipalChargesFixes() : false);

        compte.persist();

        return Response.status(201).entity(CompteResponse.fromEntity(compte)).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateCompte(@PathParam("id") UUID id, @Valid UpdateCompteRequest request) {
        Compte compte = Compte.findById(id);
        if (compte == null || !compte.getActif()) {
            return Response.status(404)
                    .entity(new ErrorResponse("Compte non trouvé"))
                    .build();
        }

        // Si ce compte devient principal, démarquer les autres
        if (request.getPrincipalChargesFixes() != null && request.getPrincipalChargesFixes()) {
            compteService.unsetAllPrincipalChargesFixes(compte.getUser());
        }

        // Mise à jour des champs modifiables
        if (request.getNom() != null) compte.setNom(request.getNom());
        if (request.getSoldeTotal() != null) compte.setSoldeTotal(request.getSoldeTotal());
        if (request.getTaux() != null) compte.setTaux(request.getTaux());
        if (request.getPlafond() != null) compte.setPlafond(request.getPlafond());
        if (request.getPrincipalChargesFixes() != null) compte.setPrincipalChargesFixes(request.getPrincipalChargesFixes());

        return Response.ok(CompteResponse.fromEntity(compte)).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteCompte(@PathParam("id") UUID id) {
        Compte compte = Compte.findById(id);
        if (compte == null) {
            return Response.status(404)
                    .entity(new ErrorResponse("Compte non trouvé"))
                    .build();
        }

        // Soft delete
        compte.setActif(false);
        return Response.ok(new MessageResponse("Compte supprimé avec succès")).build();
    }

    @GET
    @Path("/principal-charges-fixes")
    public Response getComptePrincipalChargesFixes() {
        User user = userContext.getCurrentUser();
        Compte comptePrincipal = compteService.getComptePrincipalChargesFixes(user);

        if (comptePrincipal == null) {
            return Response.status(404).entity(new ErrorResponse("Aucun compte trouvé")).build();
        }

        return Response.ok(CompteResponse.fromEntity(comptePrincipal)).build();
    }
}
