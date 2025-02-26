package edu.francis.my.sfupa.SQLite.Models;


import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

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
    @JoinColumn(name="lecturer_id")
    private Lecturer lecturer;

    public CourseEval() {}
    public CourseEval(Integer id, Classes course, Lecturer lecturer) {
        this.course = course;
        this.lecturer = lecturer;
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


}
