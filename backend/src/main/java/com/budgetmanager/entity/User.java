package com.budgetmanager.entity;

import com.budgetmanager.util.BudgetDefaults;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_created_at", columnList = "created_at")
})
@Getter
@Setter
public class User extends PanacheEntityBase {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(name = "jour_paie")
    private Integer jourPaie; // 1-31

    @Column(name = "salaire_mensuel_net", precision = 10, scale = 2)
    private BigDecimal salaireMensuelNet;

    @Column(name = "decouvert_autorise", precision = 10, scale = 2)
    private BigDecimal decouvertAutorise; // Pour jauge compte courant

    @Column(name = "objectif_compte_courant", precision = 10, scale = 2)
    private BigDecimal objectifCompteCourant; // Montant pour 100% sur la jauge compte courant

    @Column(name = "pourcentage_charges_fixes", precision = 5, scale = 2)
    private BigDecimal pourcentageChargesFixes = BudgetDefaults.DEFAULT_CHARGES_FIXES_PERCENTAGE;

    @Column(name = "pourcentage_depenses_variables", precision = 5, scale = 2)
    private BigDecimal pourcentageDepensesVariables = BudgetDefaults.DEFAULT_DEPENSES_VARIABLES_PERCENTAGE;

    @Column(name = "pourcentage_epargne", precision = 5, scale = 2)
    private BigDecimal pourcentageEpargne = BudgetDefaults.DEFAULT_EPARGNE_PERCENTAGE;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relations
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Compte> comptes;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Objectif> objectifs;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TransfertObjectif> transferts;

    // Constructeurs
    public User() {}

    public User(String nom, String prenom, Integer jourPaie, BigDecimal salaireMensuelNet, BigDecimal decouvertAutorise) {
        this.nom = nom;
        this.prenom = prenom;
        this.jourPaie = jourPaie;
        this.salaireMensuelNet = salaireMensuelNet;
        this.decouvertAutorise = decouvertAutorise;
    }

    /**
     * Validate that budget percentages sum to 100%
     * Called before persist and update operations
     */
    @PrePersist
    @PreUpdate
    private void validateBudgetPercentages() {
        if (pourcentageChargesFixes != null && pourcentageDepensesVariables != null && pourcentageEpargne != null) {
            BudgetDefaults.validateBudgetPercentages(
                    pourcentageChargesFixes,
                    pourcentageDepensesVariables,
                    pourcentageEpargne
            );
        }
    }
}