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
    // Getters and Setters
    public Long getIdGrade() {
        return idGrade;
    }

    public void setIdGrade(Long idGrade) {
        this.idGrade = idGrade;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Classes getStudentClass() {
        return studentClass;
    }

    public void setStudentClass(Classes studentClass) {
        this.studentClass = studentClass;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public boolean isRetake() {
        return retake;
    }

    public void setRetake(boolean retake) {
        this.retake = retake;
    }
}

