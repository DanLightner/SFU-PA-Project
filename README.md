# SFU PA Project

A desktop application designed to help the PA department at Saint Francis University manage their accreditation standards.

## Features
- **Accreditation Standards Management**: Track and manage accreditation requirements and standards
- **Student Data Management**: Store and manage student information and progress
- **Document Management**: Organize and maintain program documentation
- **Data Import/Export**: Import and export data using CSV files
- **User-Friendly Interface**: Modern JavaFX-based GUI for easy navigation
- **Database Integration**: SQLite database for reliable data storage
- **Cross-Platform Support**: Runs on Windows, macOS, and Linux

## Installation

### Prerequisites
- Java 17 or higher
- Maven 3.6.0 or higher
- Git

### Local Development Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/DanLightner/sfu-pa-project
   ```

2. Navigate to the project directory:
   ```bash
   cd SFU-PA-Project
   ```

3. Install dependencies and build the project:
   ```bash
   mvn clean install
   ```

4. Run the application:
   ```bash
   mvn javafx:run
   ```

### Building the Executable
To create a standalone executable:
```bash
mvn clean package
```
This will generate an executable file in the `target` directory.

## Usage
1. Launch the application using one of the methods above
2. The main interface will load with the following sections:
   - Student Management
   - Document Repository
   - Reports and Analytics

3. Use the navigation menu to access different features
4. Import data using the CSV import functionality
5. Generate reports and export data as needed

## Technologies
- **JavaFX 21.0.6**: Modern UI framework
- **Spring Boot 3.4.4**: Application framework
- **SQLite**: Database management
- **Maven**: Build and dependency management
- **OpenCSV**: CSV file handling
- **Hibernate**: Object-relational mapping
- **Launch4j**: Windows executable creation

