package edu.francis.my.sfupa.SQLite.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "Questions")
public class Questions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idQuestions")
    private Long idQuestions;

    @Column(name = "Text", nullable = false)
    private String text;

    @Column(name = "Type", nullable = false)
    private boolean type;

    @ManyToOne
    @JoinColumn(name="eval_id",nullable = false)
    private CourseEval eval;

    /*
    // Constructors
    public Questions() {
    }
    public Questions(String text, boolean type) {
        this.text = text;
        this.type = type;
        this.eval = eval;
    }

    public Long getIdQuestions() {
        return idQuestions;
    }

    public void setIdQuestions(Long idQuestions) {
        this.idQuestions = idQuestions;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean getType() {
        return type;
    }

    public void setType(boolean type) {
        this.type = type;
    }
*/

    // Constructors
    public Questions() {
    }

    public Questions(String text, boolean type) {
        this.text = text;
        this.type = type;
    }

    public Long getIdQuestions() {
        return idQuestions;
    }

    public void setIdQuestions(Long idQuestions) {
        this.idQuestions = idQuestions;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean getType() {
        return type;
    }

    public void setType(boolean type) {
        this.type = type;
    }

    public CourseEval getEval() {
        return eval;
    }

    public void setEval(CourseEval eval) {
        this.eval = eval;
    }
}
