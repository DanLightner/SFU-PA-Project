package edu.francis.my.sfupa.SQLite.Services;

import edu.francis.my.sfupa.SQLite.Models.*;
import edu.francis.my.sfupa.SQLite.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
public class CSVGrade {
    private static final Logger logger = LoggerFactory.getLogger(CSVGrade.class);

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ClassesRepository classesRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private SchoolYearRepository schoolYearRepository;

    @Transactional
    public int processGradeCSV(File csvFile, String courseCode, int semesterId, Integer yearId) throws IOException, CsvValidationException {
        int gradesBelowCount = 0;
        List<String> errors = new ArrayList<>();
        Map<Integer, String> columnMap = new HashMap<>();

        try (CSVReader csvReader = new CSVReader(new FileReader(csvFile))) {
            // Process header and find required columns
            String[] header = csvReader.readNext();
            if (header == null) {
                throw new IOException("CSV file is empty");
            }

            // Find the indices of required columns
            for (int i = 0; i < header.length; i++) {
                String columnName = header[i].trim();
                if (columnName.equalsIgnoreCase("ID")) {
                    columnMap.put(i, "ID");
                } else if (columnName.equalsIgnoreCase("Final Grade")) {
                    columnMap.put(i, "Final Grade");
                }
            }

            // Validate that we found both required columns
            if (!columnMap.containsValue("ID") || !columnMap.containsValue("Final Grade")) {
                throw new IOException("Required columns 'ID' and 'Final Grade' not found in CSV");
            }

            // Get column indices
            int idColumnIndex = getColumnIndex(columnMap, "ID");
            int gradeColumnIndex = getColumnIndex(columnMap, "Final Grade");

            String[] line;
            int rowNumber = 1; // Start after header
            while ((line = csvReader.readNext()) != null) {
                rowNumber++;
                
                // Skip completely empty lines or lines with only whitespace
                if (line.length == 0 || isEmptyRow(line)) {
                    logger.debug("Skipping empty row {}", rowNumber);
                    continue;
                }

                // Ensure the line has enough columns
                if (line.length <= Math.max(idColumnIndex, gradeColumnIndex)) {
                    logger.debug("Row {} has insufficient columns, padding with empty values", rowNumber);
                    // Create new array with required length
                    String[] paddedLine = new String[Math.max(idColumnIndex, gradeColumnIndex) + 1];
                    System.arraycopy(line, 0, paddedLine, 0, line.length);
                    // Fill remaining elements with empty strings
                    for (int i = line.length; i < paddedLine.length; i++) {
                        paddedLine[i] = "";
                    }
                    line = paddedLine;
                }

                String studentId = line[idColumnIndex].trim();
                String grade = line[gradeColumnIndex].trim();

                // Skip rows with empty student ID or grade
                if (studentId.isEmpty() || grade.isEmpty()) {
                    if (rowNumber != 2 && rowNumber != 3) { // Skip error reporting for rows 2 and 3
                        logger.debug("Row {} skipped: Empty student ID or grade", rowNumber);
                    }
                    continue;
                }

                try {
                    // Parse and validate student ID - remove any non-numeric characters
                    String cleanedId = studentId.replaceAll("[^0-9]", "");
                    if (cleanedId.isEmpty()) {
                        errors.add(String.format("Row %d: Invalid student ID format", rowNumber));
                        continue;
                    }

                    Long studentIdLong = Long.parseLong(cleanedId);
                    logger.info("Processing student ID: {}", studentIdLong); // Debug log

                    // Validate grade format
                    if (!isValidGrade(grade)) {
                        errors.add(String.format("Row %d: Invalid grade format", rowNumber));
                        continue;
                    }

                    // Count grades C and below
                    if (isGradeCOrBelow(grade)) {
                        gradesBelowCount++;
                    }

                    // Save the grade
                    saveGrade(studentIdLong, grade, courseCode, semesterId, yearId);

                } catch (NumberFormatException e) {
                    errors.add(String.format("Row %d: Invalid student ID format - %s", rowNumber, studentId));
                } catch (Exception e) {
                    errors.add(String.format("Row %d: %s", rowNumber, e.getMessage()));
                }
            }
        }

        // If there were any errors (excluding rows 2 and 3), throw an exception with all error messages
        if (!errors.isEmpty()) {
            throw new RuntimeException("Errors processing CSV:\n" + String.join("\n", errors));
        }

        return gradesBelowCount;
    }

    private int getColumnIndex(Map<Integer, String> columnMap, String columnName) {
        return columnMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(columnName))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(-1);
    }

    private boolean isValidGrade(String grade) {
        grade = grade.trim().toUpperCase();
        return grade.matches("^[ABCDF][+-]?$");
    }

    private boolean isGradeCOrBelow(String grade) {
        grade = grade.trim().toUpperCase();
        return grade.startsWith("C") || grade.equals("D+") || grade.equals("D") || 
               grade.equals("D-") || grade.equals("F");
    }

    @Transactional
    private void saveGrade(Long studentId, String grade, String courseCode, int semesterId, Integer yearId) {
        // Create or get student
        Student student = studentRepository.findById(studentId)
                .orElseGet(() -> {
                    Student newStudent = new Student();
                    newStudent.setId_student(studentId);
                    return studentRepository.save(newStudent);
                });

        // Create or get class
        Classes studentClass = classesRepository.findByClassCode_CourseCodeAndSemester_IdAndSchoolYear_IdSchoolYear(courseCode, semesterId, yearId)
                .orElseGet(() -> {
                    Classes newClass = new Classes();
                    // Get course by code
                    Course course = courseRepository.findById(courseCode)
                            .orElseThrow(() -> new RuntimeException("Course not found: " + courseCode));
                    
                    // Get semester by id
                    Semester semester = semesterRepository.findById(semesterId)
                            .orElseThrow(() -> new RuntimeException("Semester not found: " + semesterId));
                    
                    // Get school year by id
                    SchoolYear schoolYear = schoolYearRepository.findById(yearId)
                            .orElseThrow(() -> new RuntimeException("School year not found: " + yearId));
                    
                    newClass.setClassCode(course);
                    newClass.setSemester(semester);
                    newClass.setSchoolYear(schoolYear);
                    return classesRepository.save(newClass);
                });

        // Create grade
        Grade gradeEntity = new Grade();
        gradeEntity.setStudent(student);
        gradeEntity.setStudentClass(studentClass);
        gradeEntity.setGrade(grade);
        gradeEntity.setRetake(false); // Default to false for new grades
        gradeRepository.save(gradeEntity);
    }

    private boolean isEmptyRow(String[] line) {
        for (String field : line) {
            if (field != null && !field.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
