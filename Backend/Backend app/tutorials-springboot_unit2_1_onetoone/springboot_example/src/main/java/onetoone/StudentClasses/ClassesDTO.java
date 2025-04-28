package onetoone.StudentClasses;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Data Transfer Object for StudentClasses
 * Used to transfer data between client and server without exposing entity relationships
 */
public class ClassesDTO {
    private int id;
    private String className;
    private int teacherId;
    private String teacherName;
    private String location;
    private Set<ScheduleDTO> schedules = new HashSet<>();
    private Set<Integer> studentIds = new HashSet<>();

    // Default constructor
    public ClassesDTO() {
    }

    // Constructor from Entity
    public ClassesDTO(StudentClasses studentClass) {
        this.id = studentClass.getId();
        this.className = studentClass.getClassName();
        if (studentClass.getTeacher() != null) {
            this.teacherId = studentClass.getTeacher().getId();
            this.teacherName = studentClass.getTeacher().getName();
        }
        this.location = studentClass.getLocation();

        // Convert schedules
        studentClass.getSchedules().forEach(schedule -> {
            this.schedules.add(new ScheduleDTO(
                    schedule.getDayOfWeek(),
                    schedule.getStartTime(),
                    schedule.getEndTime()
            ));
        });

        // Add student IDs
        studentClass.getStudents().forEach(student -> {
            this.studentIds.add(student.getId());
        });
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Set<ScheduleDTO> getSchedules() {
        return schedules;
    }

    public void setSchedules(Set<ScheduleDTO> schedules) {
        this.schedules = schedules;
    }

    public Set<Integer> getStudentIds() {
        return studentIds;
    }

    public void setStudentIds(Set<Integer> studentIds) {
        this.studentIds = studentIds;
    }

    // Inner class for schedule
    public static class ScheduleDTO {
        private DayOfWeek dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;

        public ScheduleDTO() {
        }

        public ScheduleDTO(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
            this.dayOfWeek = dayOfWeek;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public DayOfWeek getDayOfWeek() {
            return dayOfWeek;
        }

        public void setDayOfWeek(DayOfWeek dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
        }

        public LocalTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalTime startTime) {
            this.startTime = startTime;
        }

        public LocalTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalTime endTime) {
            this.endTime = endTime;
        }
    }
}