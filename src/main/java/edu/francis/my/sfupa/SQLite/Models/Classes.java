package edu.francis.my.sfupa.SQLite.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "Classes")
public class Classes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idClass")
    private Long idClass;

    @NotBlank
    @Column(name = "Class_Code", length = 10)
    private String classCode;

    @ManyToOne
    @JoinColumn(name = "Semester_idSemester", nullable = false)
    private Semester semester;

    @ManyToOne
    @JoinColumn(name = "SchoolYear_idSchoolYear", nullable = false)
    private SchoolYear schoolYear;

    // Constructors
    public Classes() {
    }

    public Classes(String classCode, Semester semester, SchoolYear schoolYear) {
        this.classCode = classCode;
        this.semester = semester;
        this.schoolYear = schoolYear;
    }

    // Getters and Setters
    public Long getIdClass() {
        return idClass;
    }

    public void setIdClass(Long idClass) {
        this.idClass = idClass;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public SchoolYear getSchoolYear() {
        return schoolYear;
    }

    public void setSchoolYear(SchoolYear schoolYear) {
        this.schoolYear = schoolYear;
    }
}
