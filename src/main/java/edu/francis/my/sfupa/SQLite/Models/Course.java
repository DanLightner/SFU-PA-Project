package edu.francis.my.sfupa.SQLite.Models;

import com.fasterxml.jackson.annotation.JsonGetter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "Course")
public class Course {

    @Id
    @Column(name = "courseCode", length = 10)
    private String courseCode;

    @NotBlank
    @Column(name = "name", length = 50)
    private String name;
    // Getters, Setters, Constructors
    public Course(){

    }
    public Course(String courseCode, String name) {
        this.courseCode = courseCode;
        this.name = name;
    }

    public String getcourseCode() {
        return courseCode;
    }
    public void setCourseId(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
