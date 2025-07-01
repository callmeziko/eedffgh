

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CourseManager {
    private List<CourseSelection> courseSelections;
    
    public CourseManager() {
        this.courseSelections = new ArrayList<>();
    }
    
    // Add a new course selection with enhanced validation
    public boolean addCourseSelection(CourseSelection course) {
        if (!isValidCourseSelection(course)) {
            return false;
        }
        
        // Check for duplicate with null-safe comparisons
        if (courseSelections.stream().anyMatch(c -> 
            Objects.equals(Utils.cleanString(c.getStudentId()), Utils.cleanString(course.getStudentId())) && 
            Objects.equals(Utils.cleanString(c.getCourseId()), Utils.cleanString(course.getCourseId())) &&
            Objects.equals(Utils.cleanString(c.getSemester()), Utils.cleanString(course.getSemester())))) {
            return false;
        }
        
        // Normalize data before storing
        course.setStudentName(Utils.capitalizeWords(Objects.requireNonNull(course.getStudentName())));
        course.setCourseName(Utils.capitalizeWords(Objects.requireNonNull(course.getCourseName())));
        courseSelections.add(course);
        return true;
    }
    
    // Enhanced delete with null-safe case-insensitive matching
    public boolean deleteCourseSelection(String studentId, String courseId, String semester) {
        String cleanStudentId = Utils.cleanString(studentId);
        String cleanCourseId = Utils.cleanString(courseId);
        String cleanSemester = Utils.cleanString(semester);
        
        return courseSelections.removeIf(c -> 
            Objects.equals(Utils.cleanString(c.getStudentId()), cleanStudentId) && 
            Objects.equals(Utils.cleanString(c.getCourseId()), cleanCourseId) &&
            Objects.equals(Utils.cleanString(c.getSemester()), cleanSemester));
    }
    
    // Modified to prevent creating duplicate entries during update with null checks
    public boolean modifyCourseSelection(String studentId, String courseId, String semester, 
                                      CourseSelection newCourse) {
        if (!isValidCourseSelection(newCourse)) {
            return false;
        }
        
        String cleanStudentId = Utils.cleanString(studentId);
        String cleanCourseId = Utils.cleanString(courseId);
        String cleanSemester = Utils.cleanString(semester);
        
        for (int i = 0; i < courseSelections.size(); i++) {
            CourseSelection c = courseSelections.get(i);
            if (c != null && 
                Objects.equals(Utils.cleanString(c.getStudentId()), cleanStudentId) && 
                Objects.equals(Utils.cleanString(c.getCourseId()), cleanCourseId) &&
                Objects.equals(Utils.cleanString(c.getSemester()), cleanSemester)) {
                
                // Check if modification would create a duplicate with null checks
                if (courseSelections.stream().anyMatch(existing -> 
                    existing != null && existing != c &&
                    Objects.equals(Utils.cleanString(existing.getStudentId()), 
                                 Utils.cleanString(newCourse.getStudentId())) && 
                    Objects.equals(Utils.cleanString(existing.getCourseId()), 
                                 Utils.cleanString(newCourse.getCourseId())) &&
                    Objects.equals(Utils.cleanString(existing.getSemester()), 
                                 Utils.cleanString(newCourse.getSemester())))) {
                    return false;
                }
                
                // Normalize new data with null checks
                newCourse.setStudentName(Utils.capitalizeWords(Objects.requireNonNull(newCourse.getStudentName())));
                newCourse.setCourseName(Utils.capitalizeWords(Objects.requireNonNull(newCourse.getCourseName())));
                courseSelections.set(i, newCourse);
                return true;
            }
        }
        return false;
    }
    
    // Get courses by student ID with null checks
    public List<CourseSelection> getCoursesByStudentId(String studentId) {
        if (studentId == null) return new ArrayList<>();
        
        String cleanId = Utils.cleanString(studentId);
        return courseSelections.stream()
            .filter(Objects::nonNull)
            .filter(c -> Objects.equals(Utils.cleanString(c.getStudentId()), cleanId))
            .collect(Collectors.toList());
    }
    
    // Enhanced search with null-safe case-insensitive matching
    public List<CourseSelection> searchByStudent(String keyword) {
        if (keyword == null) return new ArrayList<>();
        
        String cleanKeyword = Utils.cleanString(keyword);
        return courseSelections.stream()
            .filter(Objects::nonNull)
            .filter(c -> {
                String studentName = Utils.cleanString(c.getStudentName());
                String studentId = Utils.cleanString(c.getStudentId());
                return (studentName != null && studentName.toLowerCase().contains(cleanKeyword.toLowerCase())) || 
                       (studentId != null && studentId.toLowerCase().contains(cleanKeyword.toLowerCase()));
            })
            .collect(Collectors.toList());
    }
    
    // Sort by credit with null checks
    public List<CourseSelection> sortByCredit() {
        return courseSelections.stream()
            .filter(Objects::nonNull)
            .sorted(Comparator.comparingDouble(CourseSelection::getCredit))
            .collect(Collectors.toList());
    }
    
    // Count courses by semester with null checks
    public int countCoursesBySemester(String semester) {
        if (semester == null) return 0;
        
        String cleanSemester = Utils.cleanString(semester);
        return (int) courseSelections.stream()
            .filter(Objects::nonNull)
            .filter(c -> Objects.equals(Utils.cleanString(c.getSemester()), cleanSemester))
            .count();
    }
    
    // Improved import with duplicate prevention and null checks
    public int importCourseSelections(List<CourseSelection> imported) {
        if (imported == null) return 0;
        
        int importedCount = 0;
        for (CourseSelection course : imported) {
            if (course != null && isValidCourseSelection(course) && addCourseSelection(course)) {
                importedCount++;
            }
        }
        return importedCount;
    }
    
    // Get all course selections with null check
    public List<CourseSelection> getAllCourseSelections() {
        return new ArrayList<>(courseSelections);
    }
    
    // Enhanced validation with name format checks
    private boolean isValidCourseSelection(CourseSelection course) {
        if (course == null) return false;
        
        // Basic field validations
        if (!Utils.isValidStudentId(course.getStudentId())) return false;
        if (!Utils.isValidCourseId(course.getCourseId())) return false;
        if (!Utils.isValidSemester(course.getSemester())) return false;
        if (course.getHours() <= 0) return false;
        if (course.getCredit() <= 0) return false;
        if (!Utils.isValidCourseType(course.getType())) return false;
        
        // Name format validations
        if (course.getStudentName() == null || course.getStudentName().trim().isEmpty()) return false;
        if (!Utils.isValidName(course.getStudentName())) return false;
        
        if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) return false;
        if (!Utils.isValidCourseName(course.getCourseName())) return false;
        
        return true;
    }
}