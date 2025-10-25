package com.budgetmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

@Entity
@Table(name = "charges_fixes", indexes = {
    @Index(name = "idx_charge_user_actif", columnList = "user_id,actif"),
    @Index(name = "idx_charge_compte", columnList = "compte_id"),
    @Index(name = "idx_charge_jour_prelevement", columnList = "jour_prelevement"),
    @Index(name = "idx_charge_frequence", columnList = "frequence")
})
@Getter
@Setter
public class ChargeFixe extends PanacheEntityBase {
    @Id
    @GeneratedValue
    private UUID id;

    @Version
    private Long version;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "compte_id", nullable = false)
    private Compte compte; // Compte de prélèvement

    @Column(nullable = false, length = 100)
    private String nom; // "Loyer", "EDF", "Assurance Auto"

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeTransaction categorie; // LOYER, ASSURANCE, ABONNEMENT, etc.

    @Column(name = "jour_prelevement", nullable = false)
    private Integer jourPrelevement; // 1-31, jour du mois

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FrequenceCharge frequence;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin; // Optionnel

    @Column(nullable = false)
    private Boolean actif = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructeurs
    public ChargeFixe() {}

    public ChargeFixe(User user, Compte compte, String nom, String description,
                      BigDecimal montant, TypeTransaction categorie, Integer jourPrelevement,
                      FrequenceCharge frequence, LocalDate dateDebut) {
        this.user = user;
        this.compte = compte;
        this.nom = nom;
        this.description = description;
        this.montant = montant;
        this.categorie = categorie;
        this.jourPrelevement = jourPrelevement;
        this.frequence = frequence;
        this.dateDebut = dateDebut;
    }
}