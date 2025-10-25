package com.budgetmanager.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transfert_objectifs", indexes = {
    @Index(name = "idx_transfert_user_date", columnList = "user_id,date_transfert"),
    @Index(name = "idx_transfert_objectif_source", columnList = "objectif_source_id"),
    @Index(name = "idx_transfert_objectif_dest", columnList = "objectif_destination_id"),
    @Index(name = "idx_transfert_created_at", columnList = "created_at")
})
@NamedEntityGraph(
        name = "TransfertObjectif.full",
        attributeNodes = {
                @NamedAttributeNode("objectifSource"),
                @NamedAttributeNode("objectifDestination"),
                @NamedAttributeNode(value = "compteSource", subgraph = "compte-banque"),
                @NamedAttributeNode(value = "compteDestination", subgraph = "compte-banque")
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
public class TransfertObjectif extends PanacheEntityBase {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "objectif_source_id", nullable = false)
    private Objectif objectifSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "objectif_destination_id", nullable = false)
    private Objectif objectifDestination;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compte_source_id", nullable = false)
    private Compte compteSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compte_destination_id", nullable = false)
    private Compte compteDestination;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal montant;

    @Column(length = 500)
    private String motif;

    @Column(name = "date_transfert", nullable = false)
    private LocalDate dateTransfert;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructeurs
    public TransfertObjectif() {}

    public TransfertObjectif(User user, Objectif objectifSource, Objectif objectifDestination,
                             Compte compteSource, Compte compteDestination, BigDecimal montant, String motif) {
        this.user = user;
        this.objectifSource = objectifSource;
        this.objectifDestination = objectifDestination;
        this.compteSource = compteSource;
        this.compteDestination = compteDestination;
        this.montant = montant;
        this.motif = motif;
        this.dateTransfert = LocalDate.now();
    }
}