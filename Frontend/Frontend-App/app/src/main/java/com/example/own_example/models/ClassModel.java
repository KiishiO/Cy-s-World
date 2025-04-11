package com.example.own_example.models;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a student class
 */
public class ClassModel implements Serializable {
    private int id;
    private String className;
    private int teacherId;
    private String teacherName;
    private String location;
    private List<ScheduleItem> schedules;
    private List<Integer> studentIds;

    // Default constructor for serialization
    public ClassModel() {
        this.schedules = new ArrayList<>();
        this.studentIds = new ArrayList<>();
    }

    public ClassModel(int id, String className, int teacherId, String teacherName, String location) {
        this.id = id;
        this.className = className;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.location = location;
        this.schedules = new ArrayList<>();
        this.studentIds = new ArrayList<>();
    }

    // Getters and setters
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

    public List<ScheduleItem> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<ScheduleItem> schedules) {
        this.schedules = schedules;
    }

    public void addSchedule(ScheduleItem schedule) {
        if (this.schedules == null) {
            this.schedules = new ArrayList<>();
        }
        this.schedules.add(schedule);
    }

    public List<Integer> getStudentIds() {
        return studentIds;
    }

    public void setStudentIds(List<Integer> studentIds) {
        this.studentIds = studentIds;
    }

    public void addStudentId(int studentId) {
        if (this.studentIds == null) {
            this.studentIds = new ArrayList<>();
        }
        this.studentIds.add(studentId);
    }

    /**
     * Returns a formatted string representation of the class schedule
     */
    public String getFormattedSchedule() {
        if (schedules == null || schedules.isEmpty()) {
            return "No schedule available";
        }

        StringBuilder sb = new StringBuilder();
        for (ScheduleItem schedule : schedules) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append(schedule.getDayOfWeek().toString().substring(0, 3))
                    .append(" ")
                    .append(formatTime(schedule.getStartTime()))
                    .append(" - ")
                    .append(formatTime(schedule.getEndTime()));
        }
        return sb.toString();
    }

    private String formatTime(LocalTime time) {
        String amPm = time.getHour() >= 12 ? "PM" : "AM";
        int hour = time.getHour() % 12;
        if (hour == 0) hour = 12;
        return String.format("%d:%02d %s", hour, time.getMinute(), amPm);
    }

    /**
     * Inner class representing a class schedule item
     */
    public static class ScheduleItem implements Serializable {
        private DayOfWeek dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;

        public ScheduleItem() {
        }

        public ScheduleItem(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
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