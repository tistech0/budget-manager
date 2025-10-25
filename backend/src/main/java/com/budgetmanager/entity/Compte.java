package com.budgetmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "comptes", indexes = {
    @Index(name = "idx_compte_user_actif", columnList = "user_id,actif"),
    @Index(name = "idx_compte_type", columnList = "type"),
    @Index(name = "idx_compte_banque", columnList = "banque_id")
})
@Getter
@Setter
public class Compte extends PanacheEntityBase {
    @Id
    @GeneratedValue
    private UUID id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banque_id", nullable = false)
    private Banque banque;

    @Column(nullable = false)
    private String nom; // "Mon PEA TR", "Livret A Fortuneo"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeCompte type;

    @Column(name = "solde_total", precision = 12, scale = 2, nullable = false)
    private BigDecimal soldeTotal = BigDecimal.ZERO; // Solde physique réel

    @Column(precision = 5, scale = 2)
    private BigDecimal taux; // 3.0 pour Livret A, variable pour PEA

    @Column(precision = 12, scale = 2)
    private BigDecimal plafond; // 22950 pour Livret A, null pour PEA

    @Column(name = "date_ouverture")
    private LocalDate dateOuverture;

    @Column(nullable = false)
    private Boolean actif = true;

    @Column(name = "principal_charges_fixes", nullable = false)
    private Boolean principalChargesFixes = false;

    // Relations
    @OneToMany(mappedBy = "compte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ObjectifRepartition> objectifRepartitions;

    // Constructeurs
    public Compte() {}

    public Compte(User user, Banque banque, String nom, TypeCompte type, BigDecimal soldeTotal) {
        this.user = user;
        this.banque = banque;
        this.nom = nom;
        this.type = type;
        this.soldeTotal = soldeTotal;
        this.actif = true;
    }

    // Méthodes métier (business logic)
    @JsonIgnore
    public BigDecimal getArgentLibre() {
        if (this.objectifRepartitions == null) return this.soldeTotal;

        BigDecimal totalObjectifs = this.objectifRepartitions.stream()
                .map(ObjectifRepartition::getMontantActuel)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return this.soldeTotal.subtract(totalObjectifs);
    }
}
