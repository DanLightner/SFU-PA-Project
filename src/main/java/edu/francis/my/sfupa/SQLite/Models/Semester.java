package edu.francis.my.sfupa.SQLite.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "Semester")
public class Semester {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "semester_name", nullable = false)
    private SemesterName name;

    // Constructors
    public Semester() {
    }
    public Semester(SemesterName category) {
        this.name = name;
    }
    // Getters and Setters
    public SemesterName getName() {
        return name;
    }

    public void setName(SemesterName name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
