package edu.francis.my.sfupa.SQLite.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "Grade")
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idGrade")
    private Long idGrade;

    @ManyToOne
    @JoinColumn(name = "Student_idStudent", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "Class_idClass", nullable = false)
    private Classes studentClass;

    @Column(name = "Grade", columnDefinition = "ENUM(...)")
    private String grade;

    @Column(name = "Retake", columnDefinition = "TINYINT")
    private boolean retake;

    // Constructors, Getters, and Setters...
}
