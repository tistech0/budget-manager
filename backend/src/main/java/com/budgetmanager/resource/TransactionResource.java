package com.budgetmanager.resource;

import com.budgetmanager.dto.*;
import com.budgetmanager.entity.*;
import com.budgetmanager.entity.TypeTransaction;
import com.budgetmanager.service.BankStatementParserService;
import com.budgetmanager.service.CSVBankStatementParserService;
import com.budgetmanager.service.MonthSnapshotService;
import com.budgetmanager.service.SalaireValideService;
import com.budgetmanager.service.TransactionService;
import com.budgetmanager.service.UserContext;
import com.budgetmanager.util.LazyLoadingUtil;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/api/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionResource {

    private static final Logger LOGGER = Logger.getLogger(TransactionResource.class);

    @Inject
    UserContext userContext;

    @Inject
    TransactionService transactionService;

    @Inject
    BankStatementParserService pdfParserService;

    @Inject
    CSVBankStatementParserService csvParserService;

    @Inject
    SalaireValideService salaireValideService;

    @Inject
    MonthSnapshotService monthSnapshotService;

    /**
     * GET /api/transactions
     * Récupérer toutes les transactions de l'utilisateur avec filtres optionnels
     */
    @GET
    public Response getAllTransactions(
            @QueryParam("dateDebut") String dateDebut,
            @QueryParam("dateFin") String dateFin,
            @QueryParam("type") TypeTransaction type,
            @QueryParam("compteId") UUID compteId,
            @QueryParam("objectifId") UUID objectifId,
            @QueryParam("limit") @DefaultValue("100") Integer limit
    ) {
        User user = userContext.getCurrentUser();

        List<Transaction> transactions = transactionService.getTransactionsFiltered(
                user, dateDebut, dateFin, type, compteId, objectifId, limit
        );

        // Charger les relations
        transactions.forEach(LazyLoadingUtil::initializeTransaction);

        List<TransactionResponse> transactionResponses = transactions.stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());

        return Response.ok(transactionResponses).build();
    }

    /**
     * GET /api/transactions/{id}
     * Récupérer une transaction par ID
     */
    @GET
    @Path("/{id}")
    public Response getTransaction(@PathParam("id") UUID id) {
        Transaction transaction = Transaction.findById(id);
        if (transaction == null) {
            return Response.status(404)
                    .entity(new ErrorResponse("Transaction non trouvée"))
                    .build();
        }

        LazyLoadingUtil.initializeTransaction(transaction);

        return Response.ok(TransactionResponse.fromEntity(transaction)).build();
    }

    /**
     * POST /api/transactions
     * Créer une nouvelle transaction
     */
    @POST
    @Transactional
    public Response createTransaction(@Valid CreateTransactionRequest request) {
        User user = userContext.getCurrentUser();

        try {
            Transaction transaction = transactionService.createTransaction(
                    user,
                    request.getCompteId(),
                    request.getObjectifId(),
                    request.getMontant(),
                    request.getDescription(),
                    request.getType(),
                    request.getDateTransaction()
            );

            return Response.status(201).entity(TransactionResponse.fromEntity(transaction)).build();
        } catch (NotFoundException e) {
            return Response.status(404)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * PUT /api/transactions/{id}
     * Mettre à jour une transaction
     */
    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateTransaction(@PathParam("id") UUID id, @Valid UpdateTransactionRequest request) {
        try {
            Transaction transaction = transactionService.updateTransaction(
                    id,
                    request.getDescription(),
                    request.getMontant(),
                    request.getDateTransaction()
            );

            return Response.ok(TransactionResponse.fromEntity(transaction)).build();
        } catch (NotFoundException e) {
            return Response.status(404)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * DELETE /api/transactions/{id}
     * Supprimer une transaction
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteTransaction(@PathParam("id") UUID id) {
        try {
            transactionService.deleteTransaction(id);
            return Response.ok(new MessageResponse("Transaction supprimée avec succès")).build();
        } catch (NotFoundException e) {
            return Response.status(404)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * POST /api/transactions/salaire
     * Valider le salaire mensuel ou autres revenus (action rapide)
     * Crée également un snapshot du mois précédent pour figer les données
     */
    @POST
    @Path("/salaire")
    @Transactional
    public Response validerSalaire(@Valid ValidationSalaireRequest request) {
        User user = userContext.getCurrentUser();

        try {
            // Create snapshot of the previous month before validating new salary
            String previousMonth = monthSnapshotService.getPreviousMonth(request.getMois());
            LOGGER.infof("Creating snapshot for previous month: %s", previousMonth);
            monthSnapshotService.createOrUpdateSnapshot(user, previousMonth);

            Transaction transaction = transactionService.validerSalaire(
                    user,
                    request.getCompteId(),
                    request.getMois(),
                    request.getMontant(),
                    request.getType(),
                    request.getDescription(),
                    request.getDateReception()
            );

            TypeTransaction type = request.getType() != null ? request.getType() : TypeTransaction.SALAIRE;

            return Response.status(201).entity(new ValidationSalaireResponse(
                    transaction.getId(),
                    type.name() + " validé avec succès",
                    transaction.getCompte().getSoldeTotal()
            )).build();
        } catch (NotFoundException e) {
            return Response.status(404)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(400)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * GET /api/transactions/salaire/{mois}/exists
     * Vérifie si un salaire a été validé pour un mois donné
     */
    @GET
    @Path("/salaire/{mois}/exists")
    public Response checkSalaireExists(@PathParam("mois") String mois) {
        User user = userContext.getCurrentUser();
        boolean exists = salaireValideService.existsForMois(user, mois);

        return Response.ok(Map.of(
                "exists", exists,
                "mois", mois
        )).build();
    }

    /**
     * GET /api/transactions/salaire/{mois}
     * Récupère le salaire validé pour un mois donné
     */
    @GET
    @Path("/salaire/{mois}")
    public Response getSalaireValide(@PathParam("mois") String mois) {
        User user = userContext.getCurrentUser();

        return salaireValideService.findByUserAndMois(user, mois)
                .map(salaire -> {
                    LazyLoadingUtil.initializeCompte(salaire.getCompte());
                    return Response.ok(salaire).build();
                })
                .orElse(Response.status(404)
                        .entity(new ErrorResponse("Aucun salaire validé pour " + mois))
                        .build());
    }

    /**
     * GET /api/transactions/statistiques
     * Obtenir les statistiques globales
     */
    @GET
    @Path("/statistiques")
    public Response getStatistiques(
            @QueryParam("dateDebut") String dateDebut,
            @QueryParam("dateFin") String dateFin
    ) {
        User user = userContext.getCurrentUser();

        LocalDate debut = dateDebut != null ? LocalDate.parse(dateDebut) : LocalDate.now().withDayOfMonth(1);
        LocalDate fin = dateFin != null ? LocalDate.parse(dateFin) : debut.plusMonths(1).minusDays(1);

        List<Transaction> transactions = Transaction.find(
                "user = ?1 and dateTransaction >= ?2 and dateTransaction <= ?3",
                user, debut, fin
        ).list();

        // Calculer les totaux par type
        BigDecimal totalRevenus = transactions.stream()
                .filter(t -> transactionService.isRevenu(t.getType()))
                .map(t -> t.getMontant())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalChargesFixes = transactions.stream()
                .filter(t -> transactionService.isChargeFixe(t.getType()))
                .map(t -> t.getMontant().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDepensesVariables = transactions.stream()
                .filter(t -> transactionService.isDepenseVariable(t.getType()))
                .map(t -> t.getMontant().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalEpargne = transactions.stream()
                .filter(t -> t.getType() == TypeTransaction.EPARGNE || t.getType() == TypeTransaction.INVESTISSEMENT)
                .map(t -> t.getMontant().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StatistiquesResponse stats = new StatistiquesResponse();
        stats.setDateDebut(debut.toString());
        stats.setDateFin(fin.toString());
        stats.setTotalRevenus(totalRevenus);
        stats.setTotalChargesFixes(totalChargesFixes);
        stats.setTotalDepensesVariables(totalDepensesVariables);
        stats.setTotalEpargne(totalEpargne);
        stats.setSoldeNet(totalRevenus.subtract(totalChargesFixes).subtract(totalDepensesVariables));
        stats.setNombreTransactions(transactions.size());

        return Response.ok(stats).build();
    }

    /**
     * POST /api/transactions/upload
     * Upload un relevé bancaire (PDF ou CSV) et retourne les transactions parsées
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadBankStatement(
            @org.jboss.resteasy.reactive.RestForm("file") FileUpload file,
            @org.jboss.resteasy.reactive.RestForm("compteId") UUID compteId
    ) {
        User user = userContext.getCurrentUser();

        // Vérifier le compte
        Compte compte = Compte.findById(compteId);
        if (compte == null || !compte.getActif()) {
            return Response.status(404)
                    .entity(new ErrorResponse("Compte non trouvé"))
                    .build();
        }

        String contentType = file.contentType();
        String fileName = file.fileName();

        // Determine file type by content type or extension
        boolean isPdf = "application/pdf".equals(contentType) ||
                        (fileName != null && fileName.toLowerCase().endsWith(".pdf"));
        boolean isCsv = "text/csv".equals(contentType) ||
                        "application/csv".equals(contentType) ||
                        "text/plain".equals(contentType) ||
                        (fileName != null && fileName.toLowerCase().endsWith(".csv"));

        if (!isPdf && !isCsv) {
            return Response.status(400)
                    .entity(new ErrorResponse("Le fichier doit être un PDF ou un CSV"))
                    .build();
        }

        try {
            File uploadedFile = file.filePath().toFile();
            List<ParsedTransactionDTO> parsedTransactions;

            if (isPdf) {
                parsedTransactions = pdfParserService.parseBankStatement(uploadedFile);
                LOGGER.infof("Successfully parsed %d transactions from uploaded PDF", parsedTransactions.size());
            } else {
                parsedTransactions = csvParserService.parseCSVBankStatement(uploadedFile);
                LOGGER.infof("Successfully parsed %d transactions from uploaded CSV", parsedTransactions.size());
            }

            return Response.ok(parsedTransactions).build();

        } catch (IOException e) {
            String fileType = isPdf ? "PDF" : "CSV";
            LOGGER.errorf(e, "Error parsing bank statement %s", fileType);
            return Response.status(500)
                    .entity(new ErrorResponse("Erreur lors de la lecture du fichier " + fileType + ": " + e.getMessage()))
                    .build();
        }
    }

    /**
     * POST /api/transactions/bulk
     * Créer plusieurs transactions en une seule opération
     */
    @POST
    @Path("/bulk")
    @Transactional
    public Response bulkCreateTransactions(@Valid BulkCreateTransactionRequest request) {
        User user = userContext.getCurrentUser();

        // Vérifier le compte
        Compte compte = Compte.findById(request.getCompteId());
        if (compte == null || !compte.getActif()) {
            return Response.status(404)
                    .entity(new ErrorResponse("Compte non trouvé"))
                    .build();
        }

        List<TransactionResponse> createdTransactions = new ArrayList<>();
        BigDecimal totalImpact = BigDecimal.ZERO;

        for (CreateTransactionRequest transactionRequest : request.getTransactions()) {
            // Vérifier l'objectif si fourni
            Objectif objectif = null;
            if (transactionRequest.getObjectifId() != null) {
                objectif = Objectif.findById(transactionRequest.getObjectifId());
                if (objectif == null || !objectif.getActif()) {
                    LOGGER.warnf("Objectif not found: %s", transactionRequest.getObjectifId());
                    continue; // Skip cette transaction
                }
            }

            // Créer la transaction
            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setCompte(compte);
            transaction.setObjectif(objectif);
            transaction.setMontant(transactionRequest.getMontant());
            transaction.setDescription(transactionRequest.getDescription());
            transaction.setType(transactionRequest.getType());
            transaction.setDateTransaction(transactionRequest.getDateTransaction() != null ?
                    LocalDate.parse(transactionRequest.getDateTransaction()) : LocalDate.now());
            transaction.persist();

            totalImpact = totalImpact.add(transactionRequest.getMontant());

            // Si transaction liée à un objectif avec montant positif, mettre à jour la répartition
            if (objectif != null && transactionRequest.getMontant().compareTo(BigDecimal.ZERO) > 0) {
                ObjectifRepartition repartition = ObjectifRepartition.find(
                        "objectif = ?1 and compte = ?2", objectif, compte
                ).firstResult();

                if (repartition == null) {
                    repartition = new ObjectifRepartition();
                    repartition.setObjectif(objectif);
                    repartition.setCompte(compte);
                    repartition.setMontantActuel(transactionRequest.getMontant());
                    repartition.setOrdre(1);
                    repartition.persist();
                } else {
                    repartition.setMontantActuel(repartition.getMontantActuel().add(transactionRequest.getMontant()));
                }
            }

            // Charger les relations pour la réponse
            LazyLoadingUtil.initializeTransaction(transaction);

            createdTransactions.add(TransactionResponse.fromEntity(transaction));
        }

        // Mettre à jour le solde du compte une seule fois
        compte.setSoldeTotal(compte.getSoldeTotal().add(totalImpact));

        LOGGER.infof("Successfully created %d transactions in bulk for compte %s",
                createdTransactions.size(), compte.getId());

        return Response.status(201).entity(createdTransactions).build();
    }
}

