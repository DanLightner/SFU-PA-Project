package edu.francis.my.sfupa.SQLite.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "Student")
public class Student {

    @Id
    @Column(name = "id_student")
    private Long id_student;

    // Constructors
    public Student() {
    }
    public Student(Long id_student) {
        this.id_student = id_student;
    }

    public Long getId_student() {
        return id_student;
    }

    public void setId_student(Long id_student) {
        this.id_student = id_student;
    }
}
