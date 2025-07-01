

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LoginPage extends Application {
    private static final String CREDENTIALS_FILE = "data/credentials.txt";
    private static final int MAX_ATTEMPTS = 3;
    private int loginAttempts = 0;
    private Map<String, String> credentials = new HashMap<>();
    private CourseManager courseManager;
    private FileHandler fileHandler;

    public LoginPage() {
        this.fileHandler = new FileHandler();
        this.courseManager = new CourseManager();
        loadCredentials();
        this.courseManager.importCourseSelections(fileHandler.loadData());
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Course Selection System - Login");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Label titleLabel = new Label("Course Selection System");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        
        Button loginButton = new Button("Login");
        Hyperlink registerLink = new Hyperlink("Register new account");
        Label messageLabel = new Label();
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);

        grid.add(titleLabel, 0, 0, 2, 1);
        grid.add(usernameLabel, 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(loginButton, 1, 3);
        grid.add(registerLink, 1, 4);
        grid.add(messageLabel, 1, 5);
        grid.add(progressIndicator, 1, 6);

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                showMessage(messageLabel, "Please enter both username and password", Color.RED);
                return;
            }

            if (loginAttempts >= MAX_ATTEMPTS) {
                showMessage(messageLabel, "Too many failed attempts. System locked.", Color.RED);
                return;
            }

            progressIndicator.setVisible(true);
            loginButton.setDisable(true);

            new Thread(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                javafx.application.Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    loginButton.setDisable(false);

                    if (authenticate(username, password)) {
                        showMessage(messageLabel, "Login successful!", Color.GREEN);
                        
                        Dashboard dashboard = new Dashboard(courseManager, username);
                        Stage dashboardStage = new Stage();
                        dashboard.start(dashboardStage);
                        
                        primaryStage.close();
                    } else {
                        loginAttempts++;
                        int remainingAttempts = MAX_ATTEMPTS - loginAttempts;
                        showMessage(messageLabel, 
                            String.format("Invalid credentials! %d attempts remaining", remainingAttempts), 
                            Color.RED);
                    }
                });
            }).start();
        });

        registerLink.setOnAction(e -> showRegistrationDialog(primaryStage));
        passwordField.setOnAction(e -> loginButton.fire());

        Scene scene = new Scene(grid, 400, 350);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void loadCredentials() {
        File file = new File(CREDENTIALS_FILE);
        if (!file.exists()) {
            credentials.put("admin", "admin123");
            credentials.put("student", "student123");
            saveCredentials();
            return;
        }

        try {
            List<String> lines = Files.readAllLines(Paths.get(CREDENTIALS_FILE));
            for (String line : lines) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    credentials.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
    System.err.println("Error loading credentials: " + e.getMessage());
    // Ensure default credentials exist
    credentials.put("admin", "admin123");
    credentials.put("student", "student123");
    try {
        saveCredentials();
    } catch (Exception ex) {
        System.err.println("Failed to save default credentials: " + ex.getMessage());
    }
}
    }

    private void saveCredentials() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CREDENTIALS_FILE))) {
            for (Map.Entry<String, String> entry : credentials.entrySet()) {
                writer.println(entry.getKey() + ":" + entry.getValue());
            }
        } catch (IOException e) {
            System.err.println("Error saving credentials: " + e.getMessage());
        }
    }

    private boolean authenticate(String username, String password) {
        return credentials.containsKey(username) && credentials.get(username).equals(password);
    }

    private void showRegistrationDialog(Stage owner) {
        Dialog<Map.Entry<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Register New Account");
        dialog.setHeaderText("Create a new user account");

        ButtonType registerButtonType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Confirm Password:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
                String username = usernameField.getText().trim();
                String password = passwordField.getText();
                String confirmPassword = confirmPasswordField.getText();

                if (username.isEmpty() || password.isEmpty()) {
                    showAlert("Error", "Username and password cannot be empty");
                    return null;
                }

                if (!password.equals(confirmPassword)) {
                    showAlert("Error", "Passwords do not match");
                    return null;
                }

                if (credentials.containsKey(username)) {
                    showAlert("Error", "Username already exists");
                    return null;
                }

                return new HashMap.SimpleEntry<>(username, password);
            }
            return null;
        });

        Optional<Map.Entry<String, String>> result = dialog.showAndWait();
        result.ifPresent(entry -> {
            credentials.put(entry.getKey(), entry.getValue());
            saveCredentials();
            showAlert("Success", "Account created successfully!");
        });
    }

    private void showMessage(Label label, String message, Color color) {
        label.setText(message);
        label.setTextFill(color);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}