package onetoone.CampusEvents;

import java.time.LocalDateTime;

public class CampusEvents {
    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String creator;
    private String category;
//    private String image;

    // Constructors
    public CampusEvents() {}

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
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

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

//    public String getImage() {
//        return image; }
//
//    public void setImage(String image) {
//        this.image = image; }
}