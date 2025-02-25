package edu.francis.my.sfupa.SQLite.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "Lecturer")
public class Lecturer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String Fname;
    private String Lname;
    // Getters and Setters

    public String getFName() {
        return Fname;
    }
    public void setFName(String Fname) {
        this.Fname = Fname;
    }

    public String getLName() {
        return Lname;
    }
    public void setLName(String Lname) {
        this.Lname = Lname;
    }
}


