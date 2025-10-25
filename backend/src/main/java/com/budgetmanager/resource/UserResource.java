package com.budgetmanager.resource;

import com.budgetmanager.dto.BudgetConfigRequest;
import com.budgetmanager.dto.BudgetConfigResponse;
import com.budgetmanager.dto.ErrorResponse;
import com.budgetmanager.dto.UserResponse;
import com.budgetmanager.entity.User;
import com.budgetmanager.service.UserContext;
import com.budgetmanager.util.MoneyConstants;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("/api/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserContext userContext;

    @GET
    @Path("/profile")
    public UserResponse getProfile() {
        User user = userContext.getCurrentUser();
        return UserResponse.fromEntity(user);
    }

    @POST
    @Path("/profile")
    @Transactional
    public Response createProfile(User user) {
        // Vérifier qu'il n'y a pas déjà un utilisateur
        long userCount = User.count();
        if (userCount > 0) {
            return Response.status(409)
                    .entity(new ErrorResponse("Un profil utilisateur existe déjà"))
                    .build();
        }

        user.persist();
        return Response.status(201).entity(UserResponse.fromEntity(user)).build();
    }

    @PUT
    @Path("/profile")
    @Transactional
    public UserResponse updateProfile(User userData) {
        User user = userContext.getCurrentUser();

        // Mise à jour des champs
        user.setNom(userData.getNom());
        user.setPrenom(userData.getPrenom());
        user.setJourPaie(userData.getJourPaie());
        user.setSalaireMensuelNet(userData.getSalaireMensuelNet());
        user.setDecouvertAutorise(userData.getDecouvertAutorise());
        user.setObjectifCompteCourant(userData.getObjectifCompteCourant());

        return UserResponse.fromEntity(user);
    }

    @GET
    @Path("/budget-config")
    public Response getBudgetConfig() {
        User user = userContext.getCurrentUser();

        BudgetConfigResponse config = new BudgetConfigResponse(
                user.getPourcentageChargesFixes(),
                user.getPourcentageDepensesVariables(),
                user.getPourcentageEpargne(),
                user.getSalaireMensuelNet()
        );

        return Response.ok(config).build();
    }

    @PUT
    @Path("/budget-config")
    @Transactional
    public Response updateBudgetConfig(@Valid BudgetConfigRequest request) {
        User user = userContext.getCurrentUser();

        // Validation : total doit égaler 100%
        var total = request.getPourcentageChargesFixes()
                .add(request.getPourcentageDepensesVariables())
                .add(request.getPourcentageEpargne());

        if (total.compareTo(MoneyConstants.HUNDRED) != 0) {
            return Response.status(400)
                    .entity(new ErrorResponse("La somme des pourcentages doit égaler 100%"))
                    .build();
        }

        // Mise à jour
        user.setPourcentageChargesFixes(request.getPourcentageChargesFixes());
        user.setPourcentageDepensesVariables(request.getPourcentageDepensesVariables());
        user.setPourcentageEpargne(request.getPourcentageEpargne());

        BudgetConfigResponse config = new BudgetConfigResponse(
                user.getPourcentageChargesFixes(),
                user.getPourcentageDepensesVariables(),
                user.getPourcentageEpargne(),
                user.getSalaireMensuelNet()
        );

        return Response.ok(config).build();
    }
}
