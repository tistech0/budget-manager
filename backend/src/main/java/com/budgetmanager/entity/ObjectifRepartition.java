package com.budgetmanager.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "objectif_repartitions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"objectif_id", "compte_id"}))
@NamedEntityGraph(
        name = "ObjectifRepartition.full",
        attributeNodes = {
                @NamedAttributeNode(value = "compte", subgraph = "compte-banque")
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "compte-banque",
                        attributeNodes = @NamedAttributeNode("banque")
                )
        }
)
@Getter
@Setter
public class ObjectifRepartition extends PanacheEntityBase {
    @Id
    @GeneratedValue
    private UUID id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "objectif_id", nullable = false)
    private Objectif objectif;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compte_id", nullable = false)
    private Compte compte;

    @Column(name = "montant_actuel", precision = 12, scale = 2, nullable = false)
    private BigDecimal montantActuel = BigDecimal.ZERO; // Montant sur ce compte pour cet objectif

    @Column(name = "pourcentage_cible", precision = 5, scale = 2)
    private BigDecimal pourcentageCible; // % de l'objectif sur ce compte (optionnel)

    @Column(nullable = false)
    private Integer ordre = 1; // Pour affichage ordonné

    // Constructeurs
    public ObjectifRepartition() {}

    public ObjectifRepartition(Objectif objectif, Compte compte, BigDecimal montantActuel) {
        this.objectif = objectif;
        this.compte = compte;
        this.montantActuel = montantActuel;
    }

    public ObjectifRepartition(Objectif objectif, Compte compte, BigDecimal montantActuel, BigDecimal pourcentageCible) {
        this.objectif = objectif;
        this.compte = compte;
        this.montantActuel = montantActuel;
        this.pourcentageCible = pourcentageCible;
    }
}