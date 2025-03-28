package edu.francis.my.sfupa;

import edu.francis.my.sfupa.SQLite.Services.CSVExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CSVExportServiceTest {

	/*
	@Autowired
	private CSVExportService csvExportService;

	private String originalDirectory;
	private String tempDirPath;

	@BeforeEach
	void setUp() throws Exception {
		Field field = CSVExportService.class.getDeclaredField("CSV_DIRECTORY");
		field.setAccessible(true);
		field.set(null, "test_directory");  // Set a new test value
	}


	@Test
	public void testCreateExportDirectoryIfNotExists() {
		csvExportService.createExportDirectoryIfNotExists();
		assertTrue(Files.exists(Path.of(tempDirPath)), "Export directory should be created");
	}

	@Test
	public void testExportLikertResponses() {
		String filePath = csvExportService.exportLikertResponses();
		assertNotNull(filePath, "File path should not be null");

		File file = new File(filePath);
		assertTrue(file.exists(), "CSV file should exist");
		assertTrue(file.length() > 0, "CSV file should not be empty");

		// Verify file content
		try {
			List<String> lines = Files.readAllLines(file.toPath());
			assertTrue(lines.size() > 1, "CSV should contain header and at least one data row");

			// Verify header
			String header = lines.get(0);
			assertTrue(header.contains("Course Code"), "Header should contain 'Course Code'");
			assertTrue(header.contains("Question"), "Header should contain 'Question'");
			assertTrue(header.contains("Response"), "Header should contain 'Response'");
		} catch (Exception e) {
			fail("Failed to read CSV file: " + e.getMessage());
		}
	}

	@Test
	public void testExportOpenResponses() {
		String filePath = csvExportService.exportOpenResponses();
		assertNotNull(filePath, "File path should not be null");

		File file = new File(filePath);
		assertTrue(file.exists(), "CSV file should exist");

		try {
			List<String> lines = Files.readAllLines(file.toPath());
			assertTrue(lines.size() >= 1, "CSV should contain at least the header");

			// Verify header
			String header = lines.get(0);
			assertTrue(header.contains("Course Code"), "Header should contain 'Course Code'");
			assertTrue(header.contains("Lecturer"), "Header should contain 'Lecturer'");
			assertTrue(header.contains("Question"), "Header should contain 'Question'");
		} catch (Exception e) {
			fail("Failed to read CSV file: " + e.getMessage());
		}
	}

	@Test
	public void testExportGradeDistribution() {
		String filePath = csvExportService.exportGradeDistribution();
		assertNotNull(filePath, "File path should not be null");

		File file = new File(filePath);
		assertTrue(file.exists(), "CSV file should exist");

		try {
			List<String> lines = Files.readAllLines(file.toPath());
			assertTrue(lines.size() >= 1, "CSV should contain at least the header");

			// Verify header
			String header = lines.get(0);
			assertTrue(header.contains("Course Code"), "Header should contain 'Course Code'");
			assertTrue(header.contains("Grade"), "Header should contain 'Grade'");
			assertTrue(header.contains("Count"), "Header should contain 'Count'");
		} catch (Exception e) {
			fail("Failed to read CSV file: " + e.getMessage());
		}
	}

	@Test
	public void testExportEvaluationSummary() {
		String filePath = csvExportService.exportEvaluationSummary();
		assertNotNull(filePath, "File path should not be null");

		File file = new File(filePath);
		assertTrue(file.exists(), "CSV file should exist");

		try {
			List<String> lines = Files.readAllLines(file.toPath());
			assertTrue(lines.size() >= 1, "CSV should contain at least the header");

			// Verify header
			String header = lines.get(0);
			assertTrue(header.contains("Course Code"), "Header should contain 'Course Code'");
			assertTrue(header.contains("Lecturer"), "Header should contain 'Lecturer'");
			assertTrue(header.contains("Question"), "Header should contain 'Question'");
			assertTrue(header.contains("Average Score"), "Header should contain 'Average Score'");
		} catch (Exception e) {
			fail("Failed to read CSV file: " + e.getMessage());
		}
	}

	 */
}