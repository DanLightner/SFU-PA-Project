package edu.francis.my.sfupa.SQLite.Models;

public enum SemesterName {
    SPRING(1), SUMMER(2), FALL(3), WINTER(4);

    private final int id;

    SemesterName(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static SemesterName fromString(String semester) {
        for (SemesterName semesterName : values()) {
            if (semesterName.name().equalsIgnoreCase(semester)) {
                return semesterName;
            }
        }
        throw new IllegalArgumentException("No enum constant for " + semester);
    }

}

