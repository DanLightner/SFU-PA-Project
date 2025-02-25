package edu.francis.my.sfupa.SQLite.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "Questions")
public class Questions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idQuestions")
    private Long idQuestions;

    @Column(name = "Text", columnDefinition = "MEDIUMTEXT")
    private String text;

    @Column(name = "Type", columnDefinition = "TINYINT")
    private int type;

    // Constructors
    public Questions() {
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
