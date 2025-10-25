package com.budgetmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "objectifs", indexes = {
    @Index(name = "idx_objectif_user_actif", columnList = "user_id,actif"),
    @Index(name = "idx_objectif_priorite", columnList = "priorite"),
    @Index(name = "idx_objectif_type", columnList = "type")
})
@NamedEntityGraph(
    name = "Objectif.withRepartitions",
    attributeNodes = {
        @NamedAttributeNode(value = "repartitions", subgraph = "repartition-subgraph")
    },
    subgraphs = {
        @NamedSubgraph(
            name = "repartition-subgraph",
            attributeNodes = {
                @NamedAttributeNode(value = "compte", subgraph = "compte-subgraph")
            }
        ),
        @NamedSubgraph(
            name = "compte-subgraph",
            attributeNodes = @NamedAttributeNode("banque")
        )
    }
)
@Getter
@Setter
public class Objectif extends PanacheEntityBase {
    @Id
    @GeneratedValue
    private UUID id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String nom; // "Voiture", "Apport Immo", "Épargne Sécurité"

    @Column(name = "montant_cible", precision = 12, scale = 2, nullable = false)
    private BigDecimal montantCible;

    private String couleur; // "#FF5722"

    private String icone; // "🚗"

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioriteObjectif priorite = PrioriteObjectif.NORMALE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeObjectif type = TypeObjectif.DIVERS;

    @Column(nullable = false)
    private Boolean actif = true;

    // Relations
    @OneToMany(mappedBy = "objectif", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ObjectifRepartition> repartitions;

    @OneToMany(mappedBy = "objectifSource", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TransfertObjectif> transfertsSortants;

    @OneToMany(mappedBy = "objectifDestination", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TransfertObjectif> transfertsEntrants;

    @Transient
    private BigDecimal montantActuel;

    @Transient
    private BigDecimal pourcentageProgression;

    // Constructeurs
    public Objectif() {}

    public Objectif(User user, String nom, BigDecimal montantCible, PrioriteObjectif priorite, TypeObjectif type) {
        this.user = user;
        this.nom = nom;
        this.montantCible = montantCible;
        this.priorite = priorite;
        this.type = type;
        this.actif = true;
    }

    public Objectif(User user, String nom, BigDecimal montantCible, TypeObjectif type) {
        this.user = user;
        this.nom = nom;
        this.montantCible = montantCible;
        this.type = type;
        this.priorite = PrioriteObjectif.NORMALE; // Priorité par défaut
        this.actif = true;
    }

    // Méthodes métier
    public BigDecimal getMontantActuel() {
        if (this.repartitions == null) return BigDecimal.ZERO;

        return this.repartitions.stream()
                .map(ObjectifRepartition::getMontantActuel)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getPourcentageProgression() {
        BigDecimal montantActuel = getMontantActuel();
        if (this.montantCible.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        return montantActuel
                .multiply(BigDecimal.valueOf(100))
                .divide(this.montantCible, 2, java.math.RoundingMode.HALF_UP);
    }

    public int getNiveauPriorite() {
        return this.priorite.niveau;
    }
}