
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.StageStyle;
import java.util.Optional;
import java.util.function.Predicate;

public class Utils {

    // Alert Styles
    private static final Font ALERT_HEADER_FONT = Font.font("System", FontWeight.BOLD, 14);
    private static final Font ALERT_CONTENT_FONT = Font.font("System", 13);

    // Displays a styled information alert dialog
    public static void showInfoAlert(String title, String message) {
        Alert alert = createStyledAlert(Alert.AlertType.INFORMATION, title, message);
        alert.showAndWait();
    }

    // Displays a styled error alert dialog
    public static void showErrorAlert(String title, String message) {
        Alert alert = createStyledAlert(Alert.AlertType.ERROR, title, message);
        alert.showAndWait();
    }

    // Displays a styled confirmation dialog
    public static boolean showConfirmationDialog(String title, String message) {
        Alert alert = createStyledAlert(Alert.AlertType.CONFIRMATION, title, message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // Displays a warning alert dialog
    public static void showWarningAlert(String title, String message) {
        Alert alert = createStyledAlert(Alert.AlertType.WARNING, title, message);
        alert.showAndWait();
    }

    // Creates a styled text input dialog
    public static Optional<String> showTextInputDialog(String title, String header, String content) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        dialog.getDialogPane().setStyle("-fx-font-size: 13px;");
        return dialog.showAndWait();
    }

    // Creates a styled custom dialog
    public static <T> Optional<T> showCustomDialog(Dialog<T> dialog) {
        dialog.getDialogPane().setStyle("-fx-font-size: 13px;");
        return dialog.showAndWait();
    }

    // Validation methods
    public static boolean isValidStudentId(String id) {
        return validateString(id, "\\d{6}", "Student ID must be 6 digits");
    }

    public static boolean isValidCourseId(String id) {
        return validateString(id, "[A-Za-z]{3}\\d{3}", "Course ID must be 3 letters followed by 3 digits");
    }

    public static boolean isValidSemester(String semester) {
        return validateString(semester, "\\d{4}-[FSfs]", "Semester must be in YYYY-F or YYYY-S format");
    }

    public static boolean isValidCourseType(String type) {
        return validateString(type, "(?i)exam|check", "Course type must be 'exam' or 'check'");
    }

    public static boolean isValidCredit(double credit) {
        return validateNumber(credit, d -> d > 0, "Credit must be positive");
    }

    public static boolean isValidHours(int hours) {
        return validateNumber(hours, h -> h > 0, "Hours must be positive");
    }

    // String manipulation
    public static String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) return str;
        
        StringBuilder result = new StringBuilder();
        for (String word : str.split("\\s+")) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                     .append(word.substring(1).toLowerCase())
                     .append(" ");
            }
        }
        return result.toString().trim();
    }

    public static String cleanString(String str) {
        return str != null ? str.trim().replaceAll("\\s+", " ") : null;
    }

    public static String formatCourseString(CourseSelection course) {
        return String.format(
            "Student: %s (%s)\nCourse: %s (%s)\nSemester: %s\nHours: %d | Credit: %.1f | Type: %s",
            course.getStudentName(),
            course.getStudentId(),
            course.getCourseName(),
            course.getCourseId(),
            course.getSemester(),
            course.getHours(),
            course.getCredit(),
            course.getType()
        );
    }

    // Helper methods
    private static Alert createStyledAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Apply consistent styling
        alert.getDialogPane().setStyle(
            "-fx-font-size: 13px;" +
            "-fx-padding: 20px;" +
            "-fx-min-width: 300px;"
        );
        
        return alert;
    }

    private static boolean validateString(String input, String regex, String errorMessage) {
        if (input == null || !input.matches(regex)) {
            showErrorAlert("Validation Error", errorMessage);
            return false;
        }
        return true;
    }

    private static <T extends Number> boolean validateNumber(T number, Predicate<T> validator, String errorMessage) {
        if (number == null || !validator.test(number)) {
            showErrorAlert("Validation Error", errorMessage);
            return false;
        }
        return true;
    }

    // Color utilities
    public static String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255));
    }
}