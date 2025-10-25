package com.budgetmanager.resource;

import com.budgetmanager.dto.ChargeFixeResponse;
import com.budgetmanager.dto.CreateChargeFixeRequest;
import com.budgetmanager.dto.ErrorResponse;
import com.budgetmanager.entity.ChargeFixe;
import com.budgetmanager.entity.Compte;
import com.budgetmanager.entity.User;
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

@Path("/api/charges-fixes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChargeFixeResource {

    @Inject
    UserContext userContext;

    @GET
    @Transactional  // ⭐ IMPORTANT pour le lazy loading
    public Response getAllChargesFixes(
            @QueryParam("page") @DefaultValue("0") Integer page,
            @QueryParam("limit") @DefaultValue("50") Integer limit) {
        User user = userContext.getCurrentUser();

        List<ChargeFixe> charges = ChargeFixe
                .find("user = ?1 and actif = true order by nom", user)
                .page(page, limit)
                .list();

        // ⭐ Convertir en DTOs PENDANT que la transaction est active
        List<ChargeFixeResponse> responses = charges.stream()
                .map(ChargeFixeResponse::fromEntity)
                .collect(Collectors.toList());

        return Response.ok(responses).build();
    }

    @POST
    @Transactional
    public Response createChargeFixe(@Valid CreateChargeFixeRequest request) {
        User user = userContext.getCurrentUser();

        Compte compte = Compte.findById(request.getCompteId());
        if (compte == null) {
            return Response.status(404)
                    .entity(new ErrorResponse("Compte non trouvé"))
                    .build();
        }

        ChargeFixe charge = new ChargeFixe(
                user, compte, request.getNom(), request.getDescription(),
                request.getMontant(), request.getCategorie(), request.getJourPrelevement(),
                request.getFrequence(), request.getDateDebut()
        );

        if (request.getDateFin() != null) {
            charge.setDateFin(request.getDateFin());
        }

        charge.persist();

        // ⭐ Convertir en DTO au lieu de retourner l'entité
        ChargeFixeResponse response = ChargeFixeResponse.fromEntity(charge);
        return Response.status(201).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateChargeFixe(@PathParam("id") UUID id, @Valid CreateChargeFixeRequest request) {
        ChargeFixe charge = ChargeFixe.findById(id);

        if (charge == null) {
            return Response.status(404)
                    .entity(new ErrorResponse("Charge fixe non trouvée"))
                    .build();
        }

        if (request.getCompteId() != null) {
            Compte compte = Compte.findById(request.getCompteId());
            if (compte == null) {
                return Response.status(404)
                        .entity(new ErrorResponse("Compte non trouvé"))
                        .build();
            }
            charge.setCompte(compte);
        }

        if (request.getNom() != null) charge.setNom(request.getNom());
        if (request.getDescription() != null) charge.setDescription(request.getDescription());
        if (request.getMontant() != null) charge.setMontant(request.getMontant());
        if (request.getCategorie() != null) charge.setCategorie(request.getCategorie());
        if (request.getJourPrelevement() != null) charge.setJourPrelevement(request.getJourPrelevement());
        if (request.getFrequence() != null) charge.setFrequence(request.getFrequence());
        if (request.getDateDebut() != null) charge.setDateDebut(request.getDateDebut());
        if (request.getDateFin() != null) charge.setDateFin(request.getDateFin());

        // ⭐ Convertir en DTO
        ChargeFixeResponse response = ChargeFixeResponse.fromEntity(charge);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteChargeFixe(@PathParam("id") UUID id) {
        ChargeFixe charge = ChargeFixe.findById(id);

        if (charge == null) {
            return Response.status(404)
                    .entity(new ErrorResponse("Charge fixe non trouvée"))
                    .build();
        }

        charge.setActif(false);
        return Response.ok().build();
    }
}