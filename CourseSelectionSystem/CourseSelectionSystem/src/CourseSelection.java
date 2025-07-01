

public class CourseSelection {
    private String studentId;
    private String studentName;
    private String courseId;
    private String courseName;
    private String semester;
    private int hours;
    private double credit;
    private String type; // "exam" or "check"

    public CourseSelection(String studentId, String studentName, String courseId, 
                          String courseName, String semester, int hours, 
                          double credit, String type) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.courseId = courseId;
        this.courseName = courseName;
        this.semester = semester;
        this.hours = hours;
        this.credit = credit;
        this.type = type;
    }

    // Getters
    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getSemester() {
        return semester;
    }

    public int getHours() {
        return hours;
    }

    public double getCredit() {
        return credit;
    }

    public String getType() {
        return type;
    }

    // Setters
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public void setCredit(double credit) {
        this.credit = credit;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format(
            "Student ID: %s, Name: %s, Course ID: %s, Course: %s, Semester: %s, " +
            "Hours: %d, Credit: %.1f, Type: %s",
            studentId, studentName, courseId, courseName, semester, hours, credit, type
        );
    }

    // Format for file storage
    public String toFileString() {
        return String.format(
            "%s,%s,%s,%s,%s,%d,%.1f,%s",
            studentId, studentName, courseId, courseName, semester, hours, credit, type
        );
    }

    // Create from file string
    public static CourseSelection fromFileString(String fileString) {
        String[] parts = fileString.split(",");
        if (parts.length != 8) {
            return null;
        }
        try {
            return new CourseSelection(
                parts[0].trim(),  // studentId
                parts[1].trim(),  // studentName
                parts[2].trim(),  // courseId
                parts[3].trim(),  // courseName
                parts[4].trim(),  // semester
                Integer.parseInt(parts[5].trim()),  // hours
                Double.parseDouble(parts[6].trim()),  // credit
                parts[7].trim()   // type
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }
}