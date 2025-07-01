

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileHandler {
    private static final String DATA_DIR = "data";
    private static final String DATA_FILE = DATA_DIR + "/courses.txt";
    private static final String BACKUP_FILE = DATA_DIR + "/courses_backup.txt";

    public FileHandler() {
        ensureDataDirectoryExists();
        createBackup();
    }

    private void ensureDataDirectoryExists() {
        File dataDir = new File(DATA_DIR);
       if (!dataDir.exists() && !dataDir.mkdirs()) {
    System.err.println("Warning: Failed to create data directory");
}
    }

    private void createBackup() {
        File dataFile = new File(DATA_FILE);
        if (dataFile.exists()) {
            try {
                Files.copy(Paths.get(DATA_FILE), Paths.get(BACKUP_FILE), 
                         java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.err.println("Failed to create backup: " + e.getMessage());
            }
        }
    }

    public List<CourseSelection> importFromFile(String filePath) {
        if (!fileExists(filePath)) {
            System.err.println("File not found: " + filePath);
            return new ArrayList<>();
        }

        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            return stream.map(CourseSelection::fromFileString)
                        .filter(course -> course != null)
                        .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean exportToFile(List<CourseSelection> courses, String filePath) {
        if (courses == null) {
            return false;
        }

        try {
            // Create parent directories if they don't exist
            File file = new File(filePath);
            file.getParentFile().mkdirs();

            // Write with UTF-8 encoding and proper line endings
            Files.write(Paths.get(filePath),
                  courses.stream()
                         .map(CourseSelection::toFileString)
                         .collect(Collectors.toList()),
                  StandardOpenOption.CREATE,
                  StandardOpenOption.TRUNCATE_EXISTING,
                  StandardOpenOption.WRITE);

            return true;
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
            return false;
        }
    }

    public List<CourseSelection> loadData() {
        if (!fileExists(DATA_FILE)) {
            return new ArrayList<>();
        }
        List<CourseSelection> courses = importFromFile(DATA_FILE);
        return courses != null ? courses : new ArrayList<>();
    }

    public boolean saveData(List<CourseSelection> courses) {
        if (courses == null) {
            return false;
        }

        boolean success = exportToFile(courses, DATA_FILE);
        if (success) {
            createBackup();
        }
        return success;
    }

    public boolean restoreFromBackup() {
        if (!fileExists(BACKUP_FILE)) {
            return false;
        }
        List<CourseSelection> backupData = importFromFile(BACKUP_FILE);
        return exportToFile(backupData, DATA_FILE);
    }

    public static String getDefaultDataFilePath() {
        return DATA_FILE;
    }

    public boolean fileExists(String filePath) {
        return filePath != null && new File(filePath).exists();
    }

    public List<String> readFileLines(String filePath) throws IOException {
        if (!fileExists(filePath)) {
            throw new FileNotFoundException("File not found: " + filePath);
        }
        return Files.readAllLines(Paths.get(filePath));
    }
}