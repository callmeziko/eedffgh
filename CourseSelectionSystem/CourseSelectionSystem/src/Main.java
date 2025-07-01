

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Main {
    public static void main(String[] args) {
        try {
            // Launch the JavaFX application
            Application.launch(LoginPage.class, args);
        } catch (Exception e) {
            // Handle any exceptions that occur during JavaFX initialization
            System.err.println("Failed to launch application: " + e.getMessage());
            e.printStackTrace();
            
            // Try to show an error alert if JavaFX platform is still usable
            try {
                Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Application Error");
                    alert.setHeaderText("Failed to start application");
                    alert.setContentText("An unexpected error occurred during startup. Please try again.\n\n"
                            + "Error details: " + e.getMessage());
                    alert.showAndWait();
                    Platform.exit();
                });
            } catch (Exception ex) {
                // If we can't even show the error dialog, just exit
                System.err.println("Critical error showing error dialog: " + ex.getMessage());
                System.exit(1);
            }
        }
    }
}