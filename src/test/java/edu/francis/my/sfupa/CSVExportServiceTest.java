package edu.francis.my.sfupa;

import edu.francis.my.sfupa.SQLite.Repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class CSVExportServiceTest {

	@Autowired
	private CSVExportService csvExportService;

	@Autowired
	private CourseRepository courseRepository;

	@Autowired
	private CourseEvalRepository courseEvalRepository;

	@Autowired
	private LecturerRepository lecturerRepository;

	@Autowired
	private ResponseLikertRepository responseLikertRepository;

	@Autowired
	private ResponseOpenRepository responseOpenRepository;

	@Autowired
	private GradeRepository gradeRepository;

	@Autowired
	private StudentRepository studentRepository;

	@Autowired
	private ClassesRepository classesRepository;

	private static final String CSV_DIRECTORY = "csv-exports";

	@BeforeEach
	public void setUp() {
		// Ensure export directory is clean before each test
		try {
			Files.createDirectories(Paths.get(CSV_DIRECTORY));
		} catch (Exception e) {
			// Handle setup issues
		}
	}

	@Test
	public void testExportLikertResponses() {
		String filePath = csvExportService.exportLikertResponses();

		// Check if the file was created
		assertTrue(Files.exists(Paths.get(filePath)), "CSV file for Likert responses should be created.");
	}

	@Test
	public void testExportOpenResponses() {
		String filePath = csvExportService.exportOpenResponses();

		// Check if the file was created
		assertTrue(Files.exists(Paths.get(filePath)), "CSV file for open responses should be created.");
	}

	@Test
	public void testExportGradeDistribution() {
		String filePath = csvExportService.exportGradeDistribution();

		// Check if the file was created
		assertTrue(Files.exists(Paths.get(filePath)), "CSV file for grade distribution should be created.");
	}

	@Test
	public void testExportEvaluationSummary() {
		String filePath = csvExportService.exportEvaluationSummary();

		// Check if the file was created
		assertTrue(Files.exists(Paths.get(filePath)), "CSV file for evaluation summary should be created.");
	}
}
