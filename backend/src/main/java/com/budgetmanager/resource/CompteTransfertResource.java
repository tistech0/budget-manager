package com.budgetmanager.resource;

import com.budgetmanager.dto.*;
import com.budgetmanager.entity.*;
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
import java.util.UUID;

@Path("/api/transferts/comptes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompteTransfertResource {

    @Inject
    UserContext userContext;

    /**
     * POST /api/transferts/comptes
     * Créer un transfert entre deux comptes
     * Crée automatiquement deux transactions (débit + crédit)
     */
    @POST
    @Transactional
    public Response createTransfert(@Valid CreateCompteTransfertRequest request) {
        User user = userContext.getCurrentUser();

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

        // Validation: pas de transfert vers le même compte
        if (compteSource.getId().equals(compteDestination.getId())) {
            return Response.status(400)
                    .entity(new ErrorResponse("Le compte source et destination doivent être différents"))
                    .build();
        }

        // Validation du montant
        if (request.getMontant().compareTo(BigDecimal.ZERO) <= 0) {
            return Response.status(400)
                    .entity(new ErrorResponse("Le montant doit être positif"))
                    .build();
        }

        // Vérifier le solde suffisant (optionnel - permet découvert)
        BigDecimal nouveauSoldeSource = compteSource.getSoldeTotal().subtract(request.getMontant());
        if (compteSource.getType() == TypeCompte.COMPTE_COURANT) {
            BigDecimal decouvertMax = user.getDecouvertAutorise() != null ?
                user.getDecouvertAutorise().negate() : BigDecimal.ZERO;
            if (nouveauSoldeSource.compareTo(decouvertMax) < 0) {
                return Response.status(400)
                        .entity(new ErrorResponse("Solde insuffisant (découvert autorisé dépassé)"))
                        .build();
            }
        } else if (nouveauSoldeSource.compareTo(BigDecimal.ZERO) < 0) {
            return Response.status(400)
                    .entity(new ErrorResponse("Solde insuffisant"))
                    .build();
        }

        // Générer la description si non fournie
        String description = request.getDescription();
        if (description == null || description.isBlank()) {
            description = "Transfert de " + compteSource.getNom() + " vers " + compteDestination.getNom();
        }

        // Déterminer la date
        LocalDate dateTransfert = request.getDateTransfert() != null ?
                LocalDate.parse(request.getDateTransfert()) : LocalDate.now();

        // 1. Créer la transaction de débit sur le compte source
        Transaction transactionDebit = new Transaction();
        transactionDebit.setUser(user);
        transactionDebit.setCompte(compteSource);
        transactionDebit.setMontant(request.getMontant().negate());
        transactionDebit.setDescription(description + " (débit)");
        transactionDebit.setType(TypeTransaction.VIREMENT_INTERNE);
        transactionDebit.setDateTransaction(dateTransfert);
        transactionDebit.persist();

        // 2. Créer la transaction de crédit sur le compte destination
        Transaction transactionCredit = new Transaction();
        transactionCredit.setUser(user);
        transactionCredit.setCompte(compteDestination);
        transactionCredit.setMontant(request.getMontant());
        transactionCredit.setDescription(description + " (crédit)");
        transactionCredit.setType(TypeTransaction.VIREMENT_INTERNE);
        transactionCredit.setDateTransaction(dateTransfert);
        transactionCredit.persist();

        // 3. Ajuster les soldes des comptes
        compteSource.setSoldeTotal(compteSource.getSoldeTotal().subtract(request.getMontant()));
        compteDestination.setSoldeTotal(compteDestination.getSoldeTotal().add(request.getMontant()));

        // Charger les relations pour la réponse
        LazyLoadingUtil.initializeTransaction(transactionDebit);
        LazyLoadingUtil.initializeTransaction(transactionCredit);

        // Retourner la transaction de débit comme confirmation
        return Response.status(201)
                .entity(new MessageResponse(
                    "Transfert de " + request.getMontant() + "€ effectué avec succès de " +
                    compteSource.getNom() + " vers " + compteDestination.getNom()
                ))
                .build();
    }
}
