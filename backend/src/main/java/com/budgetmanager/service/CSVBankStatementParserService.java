package com.budgetmanager.service;

import com.budgetmanager.dto.ParsedTransactionDTO;
import com.budgetmanager.entity.TypeTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour parser les relevés bancaires CSV.
 * Supporte le format BoursoBank (export CSV).
 */
@ApplicationScoped
public class CSVBankStatementParserService {

    private static final Logger LOGGER = Logger.getLogger(CSVBankStatementParserService.class);
    private static final String SEPARATOR = ";";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // CSV columns indices (BoursoBank format)
    private static final int COL_DATE_OP = 0;
    private static final int COL_DATE_VAL = 1;
    private static final int COL_LABEL = 2;
    private static final int COL_CATEGORY = 3;
    private static final int COL_CATEGORY_PARENT = 4;
    private static final int COL_SUPPLIER_FOUND = 5;
    private static final int COL_AMOUNT = 6;
    private static final int COL_COMMENT = 7;

    /**
     * Parse un fichier CSV de relevé bancaire et retourne les transactions extraites.
     */
    public List<ParsedTransactionDTO> parseCSVBankStatement(File csvFile) throws IOException {
        List<ParsedTransactionDTO> transactions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8))) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // Skip BOM if present
                if (isFirstLine) {
                    line = removeBOM(line);
                    isFirstLine = false;

                    // Skip header line
                    if (line.toLowerCase().contains("dateop") || line.toLowerCase().contains("label")) {
                        LOGGER.debug("Skipping header line");
                        continue;
                    }
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                ParsedTransactionDTO transaction = parseLine(line);
                if (transaction != null) {
                    transactions.add(transaction);
                }
            }

            LOGGER.infof("Parsed %d transactions from CSV", transactions.size());
        }

        return transactions;
    }

    /**
     * Parse une ligne CSV et retourne une transaction.
     */
    private ParsedTransactionDTO parseLine(String line) {
        try {
            String[] columns = parseCSVLine(line);

            if (columns.length < 7) {
                LOGGER.debugf("Line has insufficient columns: %s", line);
                return null;
            }

            String dateStr = columns[COL_DATE_OP].trim();
            String label = cleanString(columns[COL_LABEL]);
            String category = columns.length > COL_CATEGORY ? cleanString(columns[COL_CATEGORY]) : "";
            String categoryParent = columns.length > COL_CATEGORY_PARENT ? cleanString(columns[COL_CATEGORY_PARENT]) : "";
            String amountStr = columns[COL_AMOUNT].trim();

            if (dateStr.isEmpty() || label.isEmpty() || amountStr.isEmpty()) {
                LOGGER.debugf("Skipping line with empty required fields: %s", line);
                return null;
            }

            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            BigDecimal montant = parseAmount(amountStr);

            // Determine if debit based on amount sign
            boolean isDebit = montant.compareTo(BigDecimal.ZERO) < 0;

            // Auto-categorize based on BoursoBank category or description
            TypeTransaction type = categorizeFromBoursoBank(category, categoryParent, label, isDebit);

            return ParsedTransactionDTO.builder()
                    .date(date.toString())
                    .description(label)
                    .montant(montant)
                    .type(type)
                    .isDebit(isDebit)
                    .rawLine(line)
                    .build();

        } catch (DateTimeParseException | NumberFormatException e) {
            LOGGER.warnf("Failed to parse line: %s - Error: %s", line, e.getMessage());
            return null;
        }
    }

    /**
     * Parse une ligne CSV en gérant les guillemets.
     */
    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ';' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());

        return result.toArray(new String[0]);
    }

    /**
     * Parse un montant au format français (virgule comme séparateur décimal).
     */
    private BigDecimal parseAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Remove spaces and replace comma with dot
        String cleaned = amountStr.trim()
                .replaceAll("\\s+", "")
                .replace(",", ".");

        return new BigDecimal(cleaned);
    }

    /**
     * Supprime le BOM UTF-8 si présent.
     */
    private String removeBOM(String line) {
        if (line != null && line.startsWith("\uFEFF")) {
            return line.substring(1);
        }
        return line;
    }

    /**
     * Nettoie une chaîne (supprime les guillemets et trim).
     */
    private String cleanString(String str) {
        if (str == null) {
            return "";
        }
        return str.trim().replaceAll("^\"|\"$", "");
    }

    /**
     * Catégorise une transaction basée sur les catégories BoursoBank.
     */
    private TypeTransaction categorizeFromBoursoBank(String category, String categoryParent,
                                                      String description, boolean isDebit) {
        String cat = category != null ? category.toLowerCase() : "";
        String catParent = categoryParent != null ? categoryParent.toLowerCase() : "";
        String desc = description != null ? description.toUpperCase() : "";

        // Revenus (crédit)
        if (!isDebit) {
            if (desc.contains("SALAIRE") || desc.contains("PAIE")) {
                return TypeTransaction.SALAIRE;
            }
            if (cat.contains("remboursement")) {
                return TypeTransaction.REMBOURSEMENT;
            }
            if (cat.contains("virement") || catParent.contains("virement")) {
                return TypeTransaction.VIREMENT_INTERNE;
            }
            return TypeTransaction.AUTRE;
        }

        // Dépenses - Map BoursoBank categories to TypeTransaction

        // Alimentation
        if (cat.equals("alimentation") || cat.contains("courses")) {
            return TypeTransaction.ALIMENTATION;
        }

        // Restaurant
        if (cat.contains("restaurant") || cat.contains("bar") || cat.contains("discothèque")) {
            return TypeTransaction.RESTAURANT;
        }

        // Transport
        if (cat.contains("transport") || catParent.contains("transport") || catParent.contains("voyage")) {
            if (cat.contains("essence") || cat.contains("carburant") || cat.contains("station")) {
                return TypeTransaction.ESSENCE;
            }
            return TypeTransaction.TRANSPORT;
        }

        // Logement / Energie
        if (cat.contains("énergie") || cat.contains("energie") || cat.contains("électricité") ||
            cat.contains("gaz") || cat.contains("chauffage")) {
            return TypeTransaction.MAISON;
        }
        if (cat.contains("loyer") || cat.contains("bail")) {
            return TypeTransaction.LOYER;
        }
        if (catParent.contains("logement")) {
            return TypeTransaction.MAISON;
        }

        // Abonnements / Téléphonie
        if (cat.contains("téléphonie") || cat.contains("telephonie") ||
            cat.contains("abonnement") || cat.contains("internet")) {
            return TypeTransaction.ABONNEMENT;
        }

        // Santé
        if (cat.contains("médecin") || cat.contains("medecin") || cat.contains("pharmacie") ||
            cat.contains("santé") || cat.contains("sante") || catParent.contains("santé")) {
            return TypeTransaction.SANTE;
        }

        // Loisirs
        if (catParent.contains("loisir") || cat.contains("loisir") || cat.contains("cinéma") ||
            cat.contains("spectacle") || cat.contains("sport")) {
            return TypeTransaction.LOISIRS;
        }

        // Shopping / Vie quotidienne
        if (cat.contains("bricolage") || cat.contains("jardinage") || cat.contains("maison")) {
            return TypeTransaction.MAISON;
        }
        if (cat.contains("habillement") || cat.contains("vêtement") || cat.contains("shopping")) {
            return TypeTransaction.SHOPPING;
        }
        if (catParent.contains("vie quotidienne")) {
            // Check description for more precise categorization
            if (desc.contains("CASINO") || desc.contains("CARREFOUR") || desc.contains("LECLERC") ||
                desc.contains("MONOPRIX") || desc.contains("LIDL") || desc.contains("AUCHAN")) {
                return TypeTransaction.ALIMENTATION;
            }
            return TypeTransaction.AUTRE;
        }

        // Assurance
        if (cat.contains("assurance")) {
            return TypeTransaction.ASSURANCE;
        }

        // Animaux
        if (cat.contains("animaux") || cat.contains("animal")) {
            return TypeTransaction.AUTRE;
        }

        // Virements internes
        if (cat.contains("virement") || catParent.contains("mouvement") || catParent.contains("interne")) {
            return TypeTransaction.VIREMENT_INTERNE;
        }

        // Frais bancaires
        if (cat.contains("frais") || cat.contains("commission") || cat.contains("bancaire")) {
            return TypeTransaction.FRAIS_BANCAIRE;
        }

        // Épargne
        if (cat.contains("épargne") || cat.contains("epargne") || cat.contains("livret")) {
            return TypeTransaction.EPARGNE;
        }

        // Retrait
        if (cat.contains("retrait") || desc.contains("RETRAIT") || desc.contains("DAB")) {
            return TypeTransaction.RETRAIT_ESPECES;
        }

        // Default based on description keywords
        return categorizeFromDescription(desc);
    }

    /**
     * Catégorisation de secours basée sur la description.
     */
    private TypeTransaction categorizeFromDescription(String desc) {
        if (desc == null) {
            return TypeTransaction.AUTRE;
        }

        // Alimentation
        if (desc.contains("CARREFOUR") || desc.contains("AUCHAN") || desc.contains("LECLERC") ||
            desc.contains("INTERMARCHE") || desc.contains("CASINO") || desc.contains("LIDL") ||
            desc.contains("MONOPRIX") || desc.contains("FRANPRIX")) {
            return TypeTransaction.ALIMENTATION;
        }

        // Restaurant / Livraison
        if (desc.contains("DELIVEROO") || desc.contains("UBER EATS") || desc.contains("JUST EAT")) {
            return TypeTransaction.RESTAURANT;
        }

        // Transport
        if (desc.contains("SNCF") || desc.contains("RATP") || desc.contains("NAVIGO")) {
            return TypeTransaction.TRANSPORT;
        }

        // Abonnements connus
        if (desc.contains("NETFLIX") || desc.contains("SPOTIFY") || desc.contains("AMAZON PRIME") ||
            desc.contains("SFR") || desc.contains("ORANGE") || desc.contains("FREE") ||
            desc.contains("BOUYGUES")) {
            return TypeTransaction.ABONNEMENT;
        }

        return TypeTransaction.AUTRE;
    }
}
