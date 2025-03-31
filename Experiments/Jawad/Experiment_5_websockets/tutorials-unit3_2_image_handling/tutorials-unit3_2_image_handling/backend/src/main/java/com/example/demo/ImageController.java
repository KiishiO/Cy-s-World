package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@CrossOrigin("*") // Enable CORS for testing
public class ImageController {

    // Use an absolute path for the uploads directory
    private static String directory = System.getProperty("user.home") + File.separator + "uploads";

    @Autowired
    private ImageRepository imageRepository;

    @GetMapping(value = "/images/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody byte[] getImageById(@PathVariable int id) throws IOException {
        System.out.println("Image requested with ID: " + id);
        Image image = imageRepository.findById(id);
        if (image == null) {
            System.err.println("Image with ID " + id + " not found!");
            return null;
        }

        File imageFile = new File(image.getFilePath());
        if (!imageFile.exists()) {
            System.err.println("Image file not found at path: " + image.getFilePath());
            return null;
        }

        System.out.println("Returning image from: " + image.getFilePath());
        return Files.readAllBytes(imageFile.toPath());
    }

    @PostMapping("images")
    public String handleFileUpload(@RequestParam("image") MultipartFile imageFile) {
        System.out.println("Received upload request for file: " + imageFile.getOriginalFilename());

        try {
            // Ensure directory exists
            File dir = new File(directory);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                System.out.println("Created directory: " + dir.getAbsolutePath() + " - Success: " + created);
            }

            // Create unique filename
            String uniqueFilename = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            File destinationFile = new File(directory + File.separator + uniqueFilename);
            System.out.println("Attempting to save file to: " + destinationFile.getAbsolutePath());

            // Create parent directories if they don't exist
            if (!destinationFile.getParentFile().exists()) {
                destinationFile.getParentFile().mkdirs();
            }

            imageFile.transferTo(destinationFile);  // save file to disk
            System.out.println("File saved to: " + destinationFile.getAbsolutePath());

            Image image = new Image();
            image.setFilePath(destinationFile.getAbsolutePath());
            Image savedImage = imageRepository.save(image);
            System.out.println("Image saved to database with ID: " + savedImage.getId());

            return "File uploaded successfully: " + destinationFile.getAbsolutePath() + ", Image ID: " + savedImage.getId();
        } catch (IOException e) {
            System.err.println("Error uploading file: " + e.getMessage());
            e.printStackTrace();
            return "Failed to upload file: " + e.getMessage();
        }
    }

    @GetMapping("/latest-image")
    public int getLatestImageId() {
        int id = imageRepository.findLatestId();
        System.out.println("Latest image ID requested, returning: " + id);
        return id;
    }

    // Add a health check endpoint
    @GetMapping("/health")
    public String healthCheck() {
        System.out.println("Health check requested");
        return "Server is running!";
    }
}