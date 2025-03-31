package edu.francis.my.sfupa.SQLite.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "Grade")
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_grade")
    private Long idGrade;

    @ManyToOne
    @JoinColumn(name = "Student_id_student", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "Class_id_class", nullable = false)
    private Classes studentClass;

    @Column(name = "grade", nullable = false)
    private String grade;

    @Column(name = "retake", nullable = false)
    private boolean retake;

    public Grade() {}

    public Grade(Student student, Classes studentClass, String grade, boolean retake) {
        this.student = student;
        this.studentClass = studentClass;
        this.grade = grade;
        this.retake = retake;
    }

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

