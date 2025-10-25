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
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_user_date", columnList = "user_id,date_transaction"),
    @Index(name = "idx_transaction_compte_date", columnList = "compte_id,date_transaction"),
    @Index(name = "idx_transaction_objectif", columnList = "objectif_id"),
    @Index(name = "idx_transaction_type", columnList = "type"),
    @Index(name = "idx_transaction_created_at", columnList = "created_at")
})
@NamedEntityGraph(
        name = "Transaction.full",
        attributeNodes = {
                @NamedAttributeNode(value = "compte", subgraph = "compte-banque"),
                @NamedAttributeNode("objectif"),
                @NamedAttributeNode("transfertObjectif")
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
public class Transaction extends PanacheEntityBase {
    @Id
    @GeneratedValue
    private UUID id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compte_id", nullable = false)
    private Compte compte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "objectif_id")
    private Objectif objectif; // Optionnel, pour les transactions liées à un objectif

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal montant; // Positif = crédit, négatif = débit

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeTransaction type;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "date_transaction", nullable = false)
    private LocalDate dateTransaction;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Pour les transferts inter-objectifs
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfert_objectif_id")
    private TransfertObjectif transfertObjectif;

    // Constructeurs
    public Transaction() {}

    public Transaction(User user, Compte compte, BigDecimal montant, TypeTransaction type, String description) {
        this.user = user;
        this.compte = compte;
        this.montant = montant;
        this.type = type;
        this.description = description;
        this.dateTransaction = LocalDate.now();
    }

    public Transaction(User user, Compte compte, Objectif objectif, BigDecimal montant,
                       TypeTransaction type, String description) {
        this.user = user;
        this.compte = compte;
        this.objectif = objectif;
        this.montant = montant;
        this.type = type;
        this.description = description;
        this.dateTransaction = LocalDate.now();
    }



}