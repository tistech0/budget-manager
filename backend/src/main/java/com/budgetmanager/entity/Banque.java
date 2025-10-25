package com.budgetmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "banques")
@Getter
@Setter
public class Banque extends PanacheEntityBase {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String nom; // "Fortuneo", "Trade Republic", "BNP Paribas"

    @Column(name = "couleur_theme")
    private String couleurTheme; // "#2196F3"

    @Column(name = "logo_url")
    private String logoUrl; // "/logos/fortuneo.png"

    @Column(nullable = false)
    private Boolean actif = true;

    // Relations
    @OneToMany(mappedBy = "banque", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Compte> comptes;

    // Constructeurs
    public Banque() {}

    public Banque(String nom, String couleurTheme, String logoUrl) {
        this.nom = nom;
        this.couleurTheme = couleurTheme;
        this.logoUrl = logoUrl;
        this.actif = true;
    }
}