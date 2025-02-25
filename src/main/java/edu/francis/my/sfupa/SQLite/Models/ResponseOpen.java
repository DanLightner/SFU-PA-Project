package edu.francis.my.sfupa.SQLite.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "ResponseOpen")
public class ResponseOpen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idResponse;

    @Lob
    @Column(nullable = false)
    private String response;

    @ManyToOne
    @JoinColumn(name = "CourseEval_idCourseEval", nullable = false)
    private CourseEval courseEval;

    // Getters and Setters
    public Long getIdResponse() {
        return idResponse;
    }

    public void setIdResponse(Long idResponse) {
        this.idResponse = idResponse;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public CourseEval getCourseEval() {
        return courseEval;
    }

    public void setCourseEval(CourseEval courseEval) {
        this.courseEval = courseEval;
    }
}
