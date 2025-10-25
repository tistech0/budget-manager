package com.budgetmanager.service;

import com.budgetmanager.dto.ParsedTransactionDTO;
import com.budgetmanager.entity.TypeTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service pour parser les relevés bancaires PDF.
 * Supporte le format Société Générale.
 */
@ApplicationScoped
public class BankStatementParserService {

    private static final Logger LOGGER = Logger.getLogger(BankStatementParserService.class);

    // Pattern pour détecter le début d'une transaction (deux dates)
    private static final Pattern TRANSACTION_START_PATTERN = Pattern.compile(
            "^(\\d{2}/\\d{2}/\\d{4})\\s+(\\d{2}/\\d{2}/\\d{4})\\s+(.+)$"
    );

    // Pattern pour détecter une ligne de montant (nombre avec virgule, possiblement avec espaces)
    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "^\\s*(\\d+[\\s,.]\\d{2})\\s*$"
    );

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Parse un fichier PDF de relevé bancaire et retourne les transactions extraites.
     */
    public List<ParsedTransactionDTO> parseBankStatement(File pdfFile) throws IOException {
        List<ParsedTransactionDTO> transactions = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            LOGGER.infof("Extracted text from PDF (%d characters)", text.length());
            LOGGER.debugf("PDF text preview: %s", text.substring(0, Math.min(1000, text.length())));

            // Split par ligne et parser en mode multi-ligne
            String[] lines = text.split("\\r?\\n");
            boolean inTransactionSection = false;

            // Variables pour accumuler une transaction multi-ligne
            String currentDate = null;
            String currentDateValeur = null;
            StringBuilder currentDescription = new StringBuilder();
            String currentAmount = null;
            int currentLineNumber = 0;

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();

                // Détecter le début de la section transactions
                if (line.contains("Date") && line.contains("Valeur") &&
                    (line.contains("Nature de l'opération") || line.contains("Débit") || line.contains("Crédit"))) {
                    LOGGER.debugf("Found transaction section start at line %d: %s", i + 1, line);
                    inTransactionSection = true;
                    continue;
                }

                // Arrêter à la fin des transactions
                if (line.contains("TOTAL DES OPERATIONS") || line.contains("SOLDE CREDITEUR") ||
                    line.contains("Total des opérations")) {
                    LOGGER.debugf("Found transaction section end at line %d: %s", i + 1, line);

                    // Finaliser la transaction en cours si elle existe
                    if (currentDate != null) {
                        ParsedTransactionDTO transaction = buildTransaction(
                            currentDate, currentDateValeur, currentDescription.toString(), currentAmount, currentLineNumber
                        );
                        if (transaction != null) {
                            transactions.add(transaction);
                        }
                    }

                    inTransactionSection = false;
                    break;
                }

                if (!inTransactionSection || line.isEmpty()) {
                    continue;
                }

                // Vérifier si c'est le début d'une nouvelle transaction (deux dates)
                Matcher startMatcher = TRANSACTION_START_PATTERN.matcher(line);
                if (startMatcher.matches()) {
                    // Finaliser la transaction précédente si elle existe
                    if (currentDate != null) {
                        ParsedTransactionDTO transaction = buildTransaction(
                            currentDate, currentDateValeur, currentDescription.toString(), currentAmount, currentLineNumber
                        );
                        if (transaction != null) {
                            transactions.add(transaction);
                            LOGGER.debugf("Parsed transaction: %s - %s", transaction.getDescription(), transaction.getMontant());
                        }
                    }

                    // Démarrer une nouvelle transaction
                    currentDate = startMatcher.group(1);
                    currentDateValeur = startMatcher.group(2);
                    currentDescription = new StringBuilder(startMatcher.group(3).trim());
                    currentAmount = null;
                    currentLineNumber = i + 1;
                    LOGGER.debugf("Started new transaction at line %d: %s %s %s",
                        i + 1, currentDate, currentDateValeur, currentDescription);
                }
                // Vérifier si c'est une ligne de montant
                else if (currentDate != null) {
                    Matcher amountMatcher = AMOUNT_PATTERN.matcher(line);
                    if (amountMatcher.matches()) {
                        currentAmount = amountMatcher.group(1);
                        LOGGER.debugf("Found amount at line %d: %s", i + 1, currentAmount);
                    } else {
                        // C'est une ligne de description supplémentaire
                        if (currentDescription.length() > 0) {
                            currentDescription.append(" ");
                        }
                        currentDescription.append(line);
                        LOGGER.debugf("Added description line at %d: %s", i + 1, line);
                    }
                }
            }

            // Finaliser la dernière transaction si elle existe
            if (currentDate != null) {
                ParsedTransactionDTO transaction = buildTransaction(
                    currentDate, currentDateValeur, currentDescription.toString(), currentAmount, currentLineNumber
                );
                if (transaction != null) {
                    transactions.add(transaction);
                    LOGGER.debugf("Parsed last transaction: %s - %s", transaction.getDescription(), transaction.getMontant());
                }
            }

            LOGGER.infof("Parsed %d transactions from PDF", transactions.size());
        }

        return transactions;
    }

    /**
     * Construit une transaction à partir des données accumulées.
     */
    private ParsedTransactionDTO buildTransaction(String dateStr, String dateValeurStr,
                                                   String description, String amountStr, int lineNumber) {
        if (dateStr == null || description == null || description.isEmpty()) {
            LOGGER.debugf("Cannot build transaction: missing date or description at line %d", lineNumber);
            return null;
        }

        if (amountStr == null || amountStr.isEmpty()) {
            LOGGER.debugf("Cannot build transaction: missing amount at line %d for description: %s", lineNumber, description);
            return null;
        }

        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            BigDecimal montant = parseMontant(amountStr);

            // Déterminer si c'est un débit ou crédit basé sur la description et les mots-clés
            boolean isDebit = isDebitTransaction(description);

            // Si c'est un débit, le montant doit être négatif
            if (isDebit && montant.compareTo(BigDecimal.ZERO) > 0) {
                montant = montant.negate();
            }

            // Auto-catégoriser basé sur la description
            TypeTransaction type = categorizeTransaction(description, isDebit);

            String rawLine = String.format("%s %s %s %s", dateStr, dateValeurStr, description, amountStr);

            return ParsedTransactionDTO.builder()
                    .date(date.toString())
                    .description(description.trim())
                    .montant(montant)
                    .type(type)
                    .isDebit(isDebit)
                    .rawLine(rawLine)
                    .build();

        } catch (DateTimeParseException | NumberFormatException e) {
            LOGGER.warnf("Failed to build transaction at line %d: %s - Error: %s", lineNumber, description, e.getMessage());
            return null;
        }
    }

    /**
     * Détermine si une transaction est un débit basé sur sa description.
     */
    private boolean isDebitTransaction(String description) {
        if (description == null) {
            return true; // Par défaut, considérer comme débit
        }

        String desc = description.toUpperCase();

        // Les crédits sont typiquement des virements reçus ou remboursements
        if (desc.contains("VIR RECU") || desc.contains("VIREMENT RECU")) {
            return false;
        }
        if (desc.contains("REMBOURSEMENT") || desc.contains("REMB")) {
            return false;
        }

        // Tout le reste est considéré comme débit
        return true;
    }

    /**
     * Parse un montant (gère les espaces, virgules et points).
     */
    private BigDecimal parseMontant(String montantStr) {
        if (montantStr == null) {
            return BigDecimal.ZERO;
        }

        // Supprimer les espaces et remplacer la virgule par un point
        String cleaned = montantStr.trim()
                .replaceAll("\\s+", "")
                .replace(",", ".");

        return new BigDecimal(cleaned);
    }

    /**
     * Auto-catégorise une transaction basée sur sa description.
     */
    private TypeTransaction categorizeTransaction(String description, boolean isDebit) {
        if (description == null) {
            return TypeTransaction.AUTRE;
        }

        String desc = description.toUpperCase();

        // Revenus (crédit)
        if (!isDebit) {
            if (desc.contains("VIR") || desc.contains("VIREMENT")) {
                if (desc.contains("SALAIRE") || desc.contains("PAIE")) {
                    return TypeTransaction.SALAIRE;
                }
                return TypeTransaction.VIREMENT_INTERNE;
            }
            if (desc.contains("REMBOURSEMENT") || desc.contains("REMB")) {
                return TypeTransaction.REMBOURSEMENT;
            }
            return TypeTransaction.AUTRE;
        }

        // Dépenses (débit)

        // Alimentation
        if (desc.contains("CARREFOUR") || desc.contains("AUCHAN") || desc.contains("LECLERC") ||
            desc.contains("INTERMARCHE") || desc.contains("CASINO") || desc.contains("LIDL") ||
            desc.contains("MONOPRIX") || desc.contains("FRANPRIX") || desc.contains("SUPER") ||
            desc.contains("MARCHE")) {
            return TypeTransaction.ALIMENTATION;
        }

        // Restaurant
        if (desc.contains("RESTAURANT") || desc.contains("DELIVEROO") || desc.contains("UBER EATS") ||
            desc.contains("MCDO") || desc.contains("KFC") || desc.contains("BURGER") ||
            desc.contains("PIZZA") || desc.contains("CAFE") || desc.contains("BOULANGERIE")) {
            return TypeTransaction.RESTAURANT;
        }

        // Transport
        if (desc.contains("SNCF") || desc.contains("RATP") || desc.contains("TRANSPORTS") ||
            desc.contains("METRO") || desc.contains("BUS") || desc.contains("NAVIGO") ||
            desc.contains("PARKING") || desc.contains("PEAGE") || desc.contains("AUTOROUTE")) {
            return TypeTransaction.TRANSPORT;
        }

        // Essence
        if (desc.contains("TOTAL") || desc.contains("ESSO") || desc.contains("SHELL") ||
            desc.contains("ESSENCE") || desc.contains("CARBURANT") || desc.contains("STATION")) {
            return TypeTransaction.ESSENCE;
        }

        // Shopping
        if (desc.contains("AMAZON") || desc.contains("FNAC") || desc.contains("ZARA") ||
            desc.contains("H&M") || desc.contains("DECATHLON") || desc.contains("IKEA") ||
            desc.contains("LEROY")) {
            return TypeTransaction.SHOPPING;
        }

        // Abonnements
        if (desc.contains("ABONNEMENT") || desc.contains("NETFLIX") || desc.contains("SPOTIFY") ||
            desc.contains("ORANGE") || desc.contains("SFR") || desc.contains("FREE") ||
            desc.contains("BOUYGUES")) {
            return TypeTransaction.ABONNEMENT;
        }

        // Prélèvements récurrents
        if (desc.contains("PRELEVEMENT")) {
            if (desc.contains("LOYER") || desc.contains("BAIL")) {
                return TypeTransaction.LOYER;
            }
            if (desc.contains("ASSURANCE")) {
                return TypeTransaction.ASSURANCE;
            }
            if (desc.contains("EDF") || desc.contains("GDF") || desc.contains("EAU") ||
                desc.contains("ELECTRICITE") || desc.contains("GAZ")) {
                return TypeTransaction.MAISON;
            }
            return TypeTransaction.ABONNEMENT;
        }

        // Carte bancaire
        if (desc.contains("CARTE")) {
            return TypeTransaction.AUTRE;
        }

        // Retrait
        if (desc.contains("RETRAIT") || desc.contains("DAB") || desc.contains("ATM")) {
            return TypeTransaction.RETRAIT_ESPECES;
        }

        // Frais bancaires
        if (desc.contains("FRAIS") || desc.contains("COMMISSION") || desc.contains("COTISATION")) {
            return TypeTransaction.FRAIS_BANCAIRE;
        }

        return TypeTransaction.AUTRE;
    }
}
