package onetoone.Profiles;

import onetoone.Persons.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for handling Profile entity operations.
 */
@RestController
@RequestMapping("/profiles") // Base URL for all endpoints
public class ProfileController {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ProfilesService profileService;

    private final String success = "{\"message\":\"success\"}";
    private final String failure = "{\"message\":\"failure\"}";

    /**
     * Get all profiles.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Profiles>> getAllProfiles() {
        List<Profiles> profiles = profileService.getAllProfiles();
        if (profiles.isEmpty()) {
            return ResponseEntity.noContent().build(); // Returns 204 if no profiles exist
        }
        return ResponseEntity.ok(profiles);
    }

    /**
     * Get a profile by ID.
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Profiles> getProfileById(@PathVariable Long id) {
        Optional<Profiles> profile = profileService.getProfileById(id);
        return profile.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get profiles by person ID.
     */
    @GetMapping(value = "/person/{personId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Profiles>> getProfilesByPersonId(@PathVariable Long personId) {
        List<Profiles> profiles = profileService.getProfilesByPersonId(personId);
        if (profiles.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profiles);
    }

    /**
     * Create a new profile.
     */
    @PostMapping(path = "/new",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Profiles> createProfile(@RequestBody Profiles profile) {
        if (profile == null || profile.getName() == null || profile.getUserClasses() == null) {
            return ResponseEntity.badRequest().body(null); // 400 Bad Request if profile is invalid
        }

        // Save the profile using the service
        Profiles savedProfile = profileService.createProfile(profile);

        // Return the created profile with a 201 Created status
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProfile);
    }

    /**
     * Update an existing profile.
     */
    @PutMapping(value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Profiles> updateProfile(@PathVariable Long id, @RequestBody Profiles updatedProfile) {
        return profileService.getProfileById(id)
                .map(existingProfile -> {
                    // Update basic Profile details
                    existingProfile.setName(updatedProfile.getName());
                    existingProfile.setUserClasses(updatedProfile.getUserClasses());
                    existingProfile.setGradesFromClasses(updatedProfile.getGradesFromClasses());

                    // Check if the request contains a new Person
                    if (updatedProfile.getPerson() != null) {
                        Person updatedPerson = updatedProfile.getPerson();
                        existingProfile.setPerson(updatedPerson);
                    }

                    Profiles savedProfile = profileService.updateProfile(existingProfile);
                    return ResponseEntity.ok(savedProfile);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Delete a profile by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProfile(@PathVariable Long id) {
        if (!profileRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        profileService.deleteProfile(id);
        return ResponseEntity.ok(success);
    }

    /**
     * Add a class to a profile.
     */
    @PutMapping(value = "/{id}/addClass", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Profiles> addClass(@PathVariable Long id, @RequestParam String className) {
        return profileService.addClass(id, className)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Add a grade to a profile's class.
     */
    @PutMapping(value = "/{id}/addGrade", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Profiles> addGrade(@PathVariable Long id,
                                             @RequestParam String className,
                                             @RequestParam String grade) {
        return profileService.addGrade(id, className, grade)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}