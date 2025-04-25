package edu.francis.my.sfupa.SQLite.Models;


import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.util.List;

@Entity
@Table(name = "CourseEval")
public class CourseEval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "course_id")  // This is the foreign key in the CourseEval table
    private Classes course;  // Reference to the Clsses entity (not courseCode)

    @ManyToOne
    @JoinColumn(name="lecturer_id", nullable = true)
    private Lecturer lecturer;

    @Enumerated(EnumType.STRING)
    @Column(name = "eval_type", nullable = false)
    private EvalType evalType;

    public enum EvalType {
        INSTRUCTOR,
        GUEST_LECTURER
    }

    public CourseEval() {
        this.evalType = EvalType.INSTRUCTOR; // Default to instructor evaluation
    }

    public CourseEval(Integer id, Classes course, Lecturer lecturer, EvalType evalType) {
        this.course = course;
        this.lecturer = lecturer;
        this.evalType = evalType;
    }

    public static void saveAll(List<CourseEval> courseEvals) {
    }

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Classes getCourse() {
        return course;
    }

    public void setCourse(Classes course) {
        this.course = course;
    }

    public Lecturer getLecturer() {return lecturer; }

    public void setLecturer(Lecturer lecturer) {this.lecturer = lecturer; }

    public EvalType getEvalType() {
        return evalType;
    }

    public void setEvalType(EvalType evalType) {
        this.evalType = evalType;
    }
}
