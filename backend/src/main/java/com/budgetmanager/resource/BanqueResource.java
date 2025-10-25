package com.budgetmanager.resource;

import com.budgetmanager.dto.BanqueResponse;
import com.budgetmanager.dto.MessageResponse;
import com.budgetmanager.entity.Banque;
import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/banques")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BanqueResource {

    @GET
    @CacheResult(cacheName = "banques-list")
    public List<BanqueResponse> getAll() {
        return Banque.<Banque>find("actif = true order by nom")
                .list()
                .stream()
                .map(BanqueResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @POST
    @Transactional
    @CacheInvalidate(cacheName = "banques-list")
    public Response create(Banque banque) {
        banque.setActif(true);
        banque.persist();
        return Response.status(201).entity(BanqueResponse.fromEntity(banque)).build();
    }

    @GET
    @Path("/populate")
    @Transactional
    @CacheInvalidate(cacheName = "banques-list")
    public Response populateDefaultBanques() {
        // Vérifier si des banques existent déjà
        if (Banque.count() > 0) {
            return Response.ok(new MessageResponse("Les banques existent déjà")).build();
        }

        // Créer les banques populaires françaises
        String[][] banquesData = {
                {"Crédit Agricole", "#00A651", "/logos/credit-agricole.png"},
                {"BNP Paribas", "#00915A", "/logos/bnp-paribas.png"},
                {"Société Générale", "#E20026", "/logos/societe-generale.png"},
                {"Fortuneo", "#FF6600", "/logos/fortuneo.png"},
                {"Boursorama", "#FF6900", "/logos/boursorama.png"},
                {"Trade Republic", "#00D4AA", "/logos/trade-republic.png"},
                {"Revolut", "#0075EB", "/logos/revolut.png"},
                {"N26", "#00D4AA", "/logos/n26.png"},
                {"Linxea", "#2E5BFF", "/logos/linxea.png"},
                {"ING Direct", "#FF6200", "/logos/ing.png"}
        };

        for (String[] banqueData : banquesData) {
            Banque banque = new Banque(banqueData[0], banqueData[1], banqueData[2]);
            banque.persist();
        }

        return Response.ok(new MessageResponse("Banques initialisées avec succès")).build();
    }
}
