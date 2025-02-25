package edu.francis.my.sfupa.SQLite.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "SchoolYear")
public class SchoolYear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSchoolYear")
    private Long idSchoolYear;

    @NotBlank
    @Column(name = "Name", length = 45)
    private String name;

    public SchoolYear() {
    }

    public SchoolYear(String name) {
        this.name = name;
    }

    // Getters and setters
    public Long getIdSchoolYear() {
        return idSchoolYear;
    }

    public void setIdSchoolYear(Long idSchoolYear) {
        this.idSchoolYear = idSchoolYear;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
