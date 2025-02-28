package onetoone.Profiles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/profiles")
public class ProfileController {

    @Autowired
    private final ProfileRepository profileRepository;

    public ProfileController(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @GetMapping
    public ResponseEntity<List<Profiles>> getAllProfiles() {
        List<Profiles> profiles = profileRepository.findAll();
        if (profiles.isEmpty()) {
            return ResponseEntity.noContent().build(); // 404 Not found
        }
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Profiles> getProfileById(@PathVariable Long id) {
        Optional<Profiles> profile = profileRepository.findById(id);
        return profile.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/new")
    public ResponseEntity<Profiles> createProfile(@RequestBody Profiles profile) {
        Profiles savedProfile = profileRepository.save(profile);
        return ResponseEntity.ok(savedProfile);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Profiles> updateProfile(@PathVariable Long id, @RequestBody Profiles updatedProfile) {
        return profileRepository.findById(id).map(profile -> {
            profile.setName(updatedProfile.getName());
            profile.setUserClasses(updatedProfile.getUserClasses());
            profile.setGradesFromClasses(updatedProfile.getGradesFromClasses());
            profile.setPerson(updatedProfile.getPerson());
            Profiles savedProfile = profileRepository.save(profile);
            return ResponseEntity.ok(savedProfile);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteProfile(@PathVariable Long id) {
        return profileRepository.findById(id).map(profile -> {
            profileRepository.delete(profile);
            return ResponseEntity.noContent().build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
