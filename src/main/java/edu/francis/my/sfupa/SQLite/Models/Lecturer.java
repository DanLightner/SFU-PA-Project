package edu.francis.my.sfupa.SQLite.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "Lecturer")
public class Lecturer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String Fname;

    @NotBlank
    private String Lname;
    public Lecturer() {}
    public Lecturer(String Fname, String Lname) {
        this.Fname = Fname;
        this.Lname = Lname;
    }
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


