package com.budgetmanager.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Snapshot mensuel des finances de l'utilisateur.
 * Créé lors de la validation du salaire du mois suivant pour "figer" les données du mois précédent.
 */
@Entity
@Table(name = "month_snapshots", indexes = {
    @Index(name = "idx_snapshot_user_month", columnList = "user_id,month", unique = true)
})
@Data
@EqualsAndHashCode(callSuper = false)
public class MonthSnapshot extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 7)
    private String month; // Format: YYYY-MM (represents the budget cycle starting this month)

    // Budget cycle dates
    @Column(nullable = false)
    private String cycleStart; // Start date of budget cycle

    @Column(nullable = false)
    private String cycleEnd; // End date of budget cycle

    // Financial totals
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalRevenus = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalChargesFixes = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalDepensesVariables = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalEpargne = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal soldeCompteCourant = BigDecimal.ZERO;

    // Budget info at time of snapshot
    @Column(precision = 12, scale = 2)
    private BigDecimal budgetChargesFixes;

    @Column(precision = 12, scale = 2)
    private BigDecimal budgetDepensesVariables;

    @Column(precision = 12, scale = 2)
    private BigDecimal salaireMensuel;

    // Transaction counts
    private Integer nombreTransactions = 0;
    private Integer nombreChargesFixes = 0;
    private Integer nombreDepensesVariables = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
