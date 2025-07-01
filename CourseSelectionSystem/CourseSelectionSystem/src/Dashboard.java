

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;

public class Dashboard extends Application {
    private CourseManager courseManager;
    private String currentUser;
    private FileHandler fileHandler;

    public Dashboard(CourseManager courseManager, String username) {
        this.courseManager = courseManager;
        this.currentUser = username;
        this.fileHandler = new FileHandler();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Course Selection Management System - " + currentUser);

        // Create main menu
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Create tabs
        Tab addTab = createAddTab(primaryStage);
        Tab manageTab = createManageTab(primaryStage);
        Tab viewTab = createViewTab();
        Tab importExportTab = createImportExportTab(primaryStage);

        tabPane.getTabs().addAll(addTab, manageTab, viewTab, importExportTab);

        // Set up main scene
        Scene scene = new Scene(tabPane, 900, 650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Tab createAddTab(Stage primaryStage) {
        Tab tab = new Tab("Add Course");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));

        // Form fields with validation prompts
        TextField studentIdField = new TextField();
        studentIdField.setPromptText("6-digit number");
        
        TextField studentNameField = new TextField();
        studentNameField.setPromptText("Full name");
        
        TextField courseIdField = new TextField();
        courseIdField.setPromptText("Format: ABC123");
        
        TextField courseNameField = new TextField();
        TextField semesterField = new TextField();
        semesterField.setPromptText("Format: YYYY-S or YYYY-F");
        
        TextField hoursField = new TextField();
        hoursField.setPromptText("Positive integer");
        
        TextField creditField = new TextField();
        creditField.setPromptText("Positive number");
        
        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("exam", "check");
        typeComboBox.setValue("exam");

        // Add form to layout
        layout.getChildren().addAll(
            new Label("Student ID*:"), studentIdField,
            new Label("Student Name*:"), studentNameField,
            new Label("Course ID*:"), courseIdField,
            new Label("Course Name*:"), courseNameField,
            new Label("Semester*:"), semesterField,
            new Label("Hours*:"), hoursField,
            new Label("Credit*:"), creditField,
            new Label("Type*:"), typeComboBox
        );

        // Add button with enhanced validation
        Button addButton = new Button("Add Course");
        addButton.setOnAction(e -> {
            try {
                if (!Utils.isValidStudentId(studentIdField.getText())) {
                    Utils.showErrorAlert("Invalid Input", "Student ID must be 6 digits");
                    return;
                }
                
                if (!Utils.isValidCourseId(courseIdField.getText())) {
                    Utils.showErrorAlert("Invalid Input", "Course ID must be 3 letters followed by 3 numbers");
                    return;
                }

                CourseSelection course = new CourseSelection(
                    studentIdField.getText(),
                    Utils.capitalizeWords(studentNameField.getText()),
                    courseIdField.getText().toUpperCase(),
                    Utils.capitalizeWords(courseNameField.getText()),
                    semesterField.getText(),
                    Integer.parseInt(hoursField.getText()),
                    Double.parseDouble(creditField.getText()),
                    typeComboBox.getValue()
                );

                if (courseManager.addCourseSelection(course)) {
                    Utils.showInfoAlert("Success", "Course added successfully!");
                    clearFields(studentIdField, studentNameField, courseIdField, 
                              courseNameField, semesterField, hoursField, creditField);
                } else {
                    Utils.showErrorAlert("Error", "Failed to add course. Possible duplicate entry.");
                }
            } catch (NumberFormatException ex) {
                Utils.showErrorAlert("Error", "Please enter valid numbers for hours and credit.");
            }
        });

        layout.getChildren().add(addButton);
        tab.setContent(layout);
        return tab;
    }

    private Tab createManageTab(Stage primaryStage) {
        Tab tab = new Tab("Manage Courses");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));

        // Search section
        TextField searchField = new TextField();
        searchField.setPromptText("Enter student ID or name");
        Button searchButton = new Button("Search");
        ListView<CourseSelection> resultsList = new ListView<>();
        resultsList.setPrefHeight(400);

        // Add ProgressIndicator for search operation
        ProgressIndicator searchProgress = new ProgressIndicator();
        searchProgress.setVisible(false);
        searchProgress.setMaxSize(40, 40);

        searchButton.setOnAction(e -> {
            resultsList.getItems().clear();
            searchProgress.setVisible(true);
            
            new Thread(() -> {
                List<CourseSelection> results = courseManager.searchByStudent(searchField.getText());
                
                javafx.application.Platform.runLater(() -> {
                    searchProgress.setVisible(false);
                    if (results.isEmpty()) {
                        Utils.showInfoAlert("Search Results", "No courses found matching your criteria");
                    } else {
                        resultsList.getItems().addAll(results);
                    }
                });
            }).start();
        });

        // Management buttons
        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> {
            CourseSelection selected = resultsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (Utils.showConfirmationDialog("Confirm Delete", 
                    "Are you sure you want to delete this course selection?")) {
                    if (courseManager.deleteCourseSelection(
                        selected.getStudentId(), 
                        selected.getCourseId(),
                        selected.getSemester())) {
                        Utils.showInfoAlert("Success", "Course deleted successfully!");
                        resultsList.getItems().remove(selected);
                    } else {
                        Utils.showErrorAlert("Error", "Failed to delete course.");
                    }
                }
            } else {
                Utils.showErrorAlert("Error", "Please select a course to delete.");
            }
        });

        Button modifyButton = new Button("Modify Selected");
        modifyButton.setOnAction(e -> {
            CourseSelection selected = resultsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showModifyDialog(selected, resultsList, primaryStage);
            } else {
                Utils.showErrorAlert("Error", "Please select a course to modify.");
            }
        });

        HBox buttonBox = new HBox(10, deleteButton, modifyButton);

        layout.getChildren().addAll(
            new Label("Search by Student ID or Name:"),
            new HBox(10, searchField, searchButton, searchProgress),
            new Label("Results:"),
            resultsList,
            buttonBox
        );

        tab.setContent(layout);
        return tab;
    }

    private Tab createViewTab() {
        Tab tab = new Tab("View Courses");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));

        // View options
        Button viewAllButton = new Button("View All Courses");
        Button sortByCreditButton = new Button("Sort by Credit");
        TextField semesterField = new TextField();
        semesterField.setPromptText("e.g., 2023-F");
        Button countBySemesterButton = new Button("Count by Semester");

        // Results display
        TextArea resultsArea = new TextArea();
        resultsArea.setEditable(false);
        resultsArea.setPrefHeight(500);

        // Add ProgressIndicator for potentially long operations
        ProgressIndicator viewProgress = new ProgressIndicator();
        viewProgress.setVisible(false);
        viewProgress.setMaxSize(40, 40);

        viewAllButton.setOnAction(e -> {
            resultsArea.clear();
            viewProgress.setVisible(true);
            
            new Thread(() -> {
                List<CourseSelection> courses = courseManager.getAllCourseSelections();
                
                javafx.application.Platform.runLater(() -> {
                    viewProgress.setVisible(false);
                    if (courses.isEmpty()) {
                        resultsArea.setText("No courses available.");
                    } else {
                        courses.forEach(c -> resultsArea.appendText(c.toString() + "\n\n"));
                    }
                });
            }).start();
        });

        sortByCreditButton.setOnAction(e -> {
            resultsArea.clear();
            viewProgress.setVisible(true);
            
            new Thread(() -> {
                List<CourseSelection> sortedCourses = courseManager.sortByCredit();
                
                javafx.application.Platform.runLater(() -> {
                    viewProgress.setVisible(false);
                    if (sortedCourses.isEmpty()) {
                        resultsArea.setText("No courses available.");
                    } else {
                        sortedCourses.forEach(c -> resultsArea.appendText(c.toString() + "\n\n"));
                    }
                });
            }).start();
        });

        countBySemesterButton.setOnAction(e -> {
            resultsArea.clear();
            viewProgress.setVisible(true);
            
            new Thread(() -> {
                int count = courseManager.countCoursesBySemester(semesterField.getText());
                List<CourseSelection> semesterCourses = courseManager.getAllCourseSelections().stream()
                    .filter(c -> c.getSemester().equalsIgnoreCase(semesterField.getText()))
                    .toList();
                
                javafx.application.Platform.runLater(() -> {
                    viewProgress.setVisible(false);
                    resultsArea.setText("Number of courses in semester " + semesterField.getText() + 
                                      ": " + count + "\n\n");
                    semesterCourses.forEach(c -> resultsArea.appendText(c.toString() + "\n\n"));
                });
            }).start();
        });

        layout.getChildren().addAll(
            new HBox(10, viewAllButton, sortByCreditButton, viewProgress),
            new HBox(10, new Label("Semester:"), semesterField, countBySemesterButton),
            new Label("Results:"),
            resultsArea
        );

        tab.setContent(layout);
        return tab;
    }

    private Tab createImportExportTab(Stage primaryStage) {
        Tab tab = new Tab("Import/Export");
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));

        // Import section
        Label importLabel = new Label("Import Courses from File:");
        Button importButton = new Button("Browse and Import");
        
        // Progress indicator for import/export
        ProgressIndicator ioProgress = new ProgressIndicator();
        ioProgress.setVisible(false);
        ioProgress.setMaxSize(40, 40);

        importButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Course Data File");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            File file = fileChooser.showOpenDialog(primaryStage);
            
            if (file != null) {
                ioProgress.setVisible(true);
                
                new Thread(() -> {
                    List<CourseSelection> imported = fileHandler.importFromFile(file.getAbsolutePath());
                    int added = courseManager.importCourseSelections(imported);
                    
                    javafx.application.Platform.runLater(() -> {
                        ioProgress.setVisible(false);
                        Utils.showInfoAlert("Import Complete", 
                            "Successfully imported " + added + " courses.\n" +
                            (imported.size() - added) + " duplicates were skipped.");
                    });
                }).start();
            }
        });

        // Export section
        Label exportLabel = new Label("Export All Courses to File:");
        Button exportButton = new Button("Browse and Export");
        exportButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Course Data");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            File file = fileChooser.showSaveDialog(primaryStage);
            
            if (file != null) {
                ioProgress.setVisible(true);
                
                new Thread(() -> {
                    boolean success = fileHandler.exportToFile(
                        courseManager.getAllCourseSelections(), 
                        file.getAbsolutePath());
                    
                    javafx.application.Platform.runLater(() -> {
                        ioProgress.setVisible(false);
                        if (success) {
                            Utils.showInfoAlert("Export Complete", 
                                "All courses exported successfully to:\n" + file.getAbsolutePath());
                        } else {
                            Utils.showErrorAlert("Export Failed", 
                                "Failed to export courses to the selected file.");
                        }
                    });
                }).start();
            }
        });

        layout.getChildren().addAll(
            importLabel, new HBox(10, importButton, ioProgress),
            new Separator(),
            exportLabel, exportButton
        );

        tab.setContent(layout);
        return tab;
    }

    private void showModifyDialog(CourseSelection course, ListView<CourseSelection> listView, Stage primaryStage) {
        Dialog<CourseSelection> dialog = new Dialog<>();
        dialog.setTitle("Modify Course");
        dialog.setHeaderText("Edit course details for: " + course.getStudentName());

        // Set up buttons
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField studentIdField = new TextField(course.getStudentId());
        studentIdField.setDisable(true); // Student ID shouldn't be changed
        
        TextField studentNameField = new TextField(course.getStudentName());
        TextField courseIdField = new TextField(course.getCourseId());
        TextField courseNameField = new TextField(course.getCourseName());
        TextField semesterField = new TextField(course.getSemester());
        TextField hoursField = new TextField(String.valueOf(course.getHours()));
        TextField creditField = new TextField(String.valueOf(course.getCredit()));
        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("exam", "check");
        typeComboBox.setValue(course.getType());

        grid.add(new Label("Student ID:"), 0, 0);
        grid.add(studentIdField, 1, 0);
        grid.add(new Label("Student Name*:"), 0, 1);
        grid.add(studentNameField, 1, 1);
        grid.add(new Label("Course ID*:"), 0, 2);
        grid.add(courseIdField, 1, 2);
        grid.add(new Label("Course Name*:"), 0, 3);
        grid.add(courseNameField, 1, 3);
        grid.add(new Label("Semester*:"), 0, 4);
        grid.add(semesterField, 1, 4);
        grid.add(new Label("Hours*:"), 0, 5);
        grid.add(hoursField, 1, 5);
        grid.add(new Label("Credit*:"), 0, 6);
        grid.add(creditField, 1, 6);
        grid.add(new Label("Type*:"), 0, 7);
        grid.add(typeComboBox, 1, 7);

        dialog.getDialogPane().setContent(grid);

        // Result converter
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    if (!Utils.isValidCourseId(courseIdField.getText())) {
                        Utils.showErrorAlert("Invalid Input", "Course ID must be 3 letters followed by 3 numbers");
                        return null;
                    }

                    return new CourseSelection(
                        studentIdField.getText(),
                        Utils.capitalizeWords(studentNameField.getText()),
                        courseIdField.getText().toUpperCase(),
                        Utils.capitalizeWords(courseNameField.getText()),
                        semesterField.getText(),
                        Integer.parseInt(hoursField.getText()),
                        Double.parseDouble(creditField.getText()),
                        typeComboBox.getValue()
                    );
                } catch (NumberFormatException e) {
                    Utils.showErrorAlert("Error", "Please enter valid numbers for hours and credit.");
                    return null;
                }
            }
            return null;
        });

        // Handle result
        dialog.showAndWait().ifPresent(modifiedCourse -> {
            if (modifiedCourse != null) {
                if (courseManager.modifyCourseSelection(
                    course.getStudentId(), 
                    course.getCourseId(),
                    course.getSemester(),
                    modifiedCourse)) {
                    Utils.showInfoAlert("Success", "Course modified successfully!");
                    listView.getItems().remove(course);
                    listView.getItems().add(modifiedCourse);
                } else {
                    Utils.showErrorAlert("Error", "Failed to modify course. Possible duplicate entry.");
                }
            }
        });
    }

    private void clearFields(TextField... fields) {
        for (TextField field : fields) {
            field.clear();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}