package onetoone.CampusEvents;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import lombok.Data;

@Entity
@Table(name = "campus_events")
@Data
public class CampusEvents {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String location;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime = LocalDateTime.now();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "creator", nullable = false)
    private String creator;

    @Column
    private String category;

    // Default constructor
    public CampusEvents() {}

    // Constructor with parameters
    public CampusEvents(String title, String description, String location,
                        LocalDateTime startTime, LocalDateTime endTime, String creator,
                        String category) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.creator = creator;
        this.category = category;
    }

    // Full constructor with ID
    public CampusEvents(Long id, String title, String description, String location,
                        LocalDateTime startTime, LocalDateTime endTime, String creator,
                        String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.creator = creator;
        this.category = category;
    }

    // Getters and setters
    public Long getId() {
        return id; }

    public void setId(Long id) {
        this.id = id; }

    public String getTitle() {
        return title; }

    public void setTitle(String title) {
        this.title = title; }

    public String getDescription() {
        return description; }

    public void setDescription(String description) {
        this.description = description; }

    public String getLocation() {
        return location; }

    public void setLocation(String location) {
        this.location = location; }

    public LocalDateTime getStartTime() {
        return startTime; }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime; }

    public LocalDateTime getEndTime() {
        return endTime; }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime; }

    public String getCreator() {
        return creator; }

    public void setCreator(String creator) {
        this.creator = creator; }

    public String getCategory() {
        return category; }

    public void setCategory(String category) {
        this.category = category; }
}