package com.michaelhope.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "software_engineer")
@Getter
@Setter
@NoArgsConstructor
public class SoftwareEngineer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "software_engineer_technology",
        joinColumns = @JoinColumn(name = "software_engineer_id"),
        inverseJoinColumns = @JoinColumn(name = "technology_id"),
        uniqueConstraints = @UniqueConstraint(
            name = "uk_software_engineer_technology",
            columnNames = {"software_engineer_id", "technology_id"}
        )
    )
    private Set<Technology> technologies = new HashSet<>();

    @Column(nullable = false)
    private Integer aggregateVersion = 0;

    public SoftwareEngineer(Integer id, String name, Set<Technology> technologies) {
        this.id = id;
        this.name = name;
        this.technologies = technologies == null ? new HashSet<>() : new HashSet<>(technologies);
    }

    public SoftwareEngineer(Integer id, String name, String... technologyNames) {
        this.id = id;
        this.name = name;
        this.technologies = new HashSet<>();
        for (String technologyName : technologyNames) {
            String normalizedName = technologyName.toLowerCase(java.util.Locale.ROOT);
            this.technologies.add(new Technology(null, technologyName, normalizedName));
        }
    }

}
