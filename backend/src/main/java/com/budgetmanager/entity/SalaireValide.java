package com.budgetmanager.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Salaire validé par mois
 * Permet de tracker quel mois a un salaire validé et sert de référence pour le dashboard
 * Un seul salaire validé par utilisateur par mois
 */
@Entity
@Table(name = "salaires_valides",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_salaire_user_mois", columnNames = {"user_id", "mois"})
    },
    indexes = {
        @Index(name = "idx_salaire_valide_user_mois", columnList = "user_id,mois"),
        @Index(name = "idx_salaire_valide_mois", columnList = "mois")
    }
)
@Getter
@Setter
public class SalaireValide extends PanacheEntityBase {
    @Id
    @GeneratedValue
    private UUID id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Mois du salaire au format YYYY-MM (ex: "2025-01")
     */
    @Column(nullable = false, length = 7)
    private String mois;

    /**
     * Montant du salaire validé
     */
    @Column(name = "montant_salaire", precision = 12, scale = 2, nullable = false)
    private BigDecimal montantSalaire;

    /**
     * Date à laquelle le salaire a été reçu/validé
     */
    @Column(name = "date_reception", nullable = false)
    private LocalDate dateReception;

    /**
     * Compte sur lequel le salaire a été versé
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compte_id", nullable = false)
    private Compte compte;

    /**
     * Description du salaire
     */
    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructeurs
    public SalaireValide() {}

    public SalaireValide(User user, String mois, BigDecimal montantSalaire,
                         LocalDate dateReception, Compte compte, String description) {
        this.user = user;
        this.mois = mois;
        this.montantSalaire = montantSalaire;
        this.dateReception = dateReception;
        this.compte = compte;
        this.description = description;
    }
}
