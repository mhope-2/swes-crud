package com.michaelhope.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(
    name = "technology",
    uniqueConstraints = @UniqueConstraint(name = "uk_technology_normalized_name", columnNames = "normalized_name")
)
@Getter
@Setter
@NoArgsConstructor
public class Technology {

    public static final int MAX_NAME_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String name;

    @Column(name = "normalized_name", nullable = false, length = MAX_NAME_LENGTH, unique = true)
    private String normalizedName;

    public Technology(Integer id, String name, String normalizedName) {
        this.id = id;
        this.name = name;
        this.normalizedName = normalizedName;
    }

    public Technology(String name, String normalizedName) {
        this.name = name;
        this.normalizedName = normalizedName;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Technology technology)) {
            return false;
        }
        if (normalizedName != null && technology.normalizedName != null) {
            return normalizedName.equals(technology.normalizedName);
        }
        return id != null && id.equals(technology.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(normalizedName != null ? normalizedName : id);
    }
}
