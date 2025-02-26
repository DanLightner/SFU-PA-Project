package edu.francis.my.sfupa.SQLite.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "Student")
public class Student {

    @Id
    @Column(name = "idStudent")
    private Long idStudent;

    // Constructors
    public Student() {
    }
    public Student(Long idStudent) {
        this.idStudent = idStudent;
    }

    public Long getIdStudent() {
        return idStudent;
    }

    public void setIdStudent(Long idStudent) {
        this.idStudent = idStudent;
    }
}
