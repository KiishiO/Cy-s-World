package onetoone.Profiles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProfilesService {

    @Autowired
    private ProfileRepository profileRepository;

    /**
     * Get all profiles
     */
    public List<Profiles> getAllProfiles() {
        return profileRepository.findAll();
    }

    /**
     * Get a profile by ID
     */
    public Optional<Profiles> getProfileById(Long id) {
        return profileRepository.findById(id);
    }

    /**
     * Get profiles by person ID
     */
    public List<Profiles> getProfilesByPersonId(Long personId) {
        return profileRepository.findByPersonId(personId);
    }

    /**
     * Create a new profile
     */
    public Profiles createProfile(Profiles profile) {
        return profileRepository.save(profile);
    }

    /**
     * Update an existing profile
     */
    public Profiles updateProfile(Profiles profile) {
        return profileRepository.save(profile);
    }

    /**
     * Delete a profile
     */
    public void deleteProfile(Long id) {
        profileRepository.deleteById(id);
    }

    /**
     * Add a class to a profile
     */
    @Transactional
    public Optional<Profiles> addClass(Long profileId, String className) {
        return profileRepository.findById(profileId)
                .map(profile -> {
                    profile.getUserClasses().add(className);
                    return profileRepository.save(profile);
                });
    }

    /**
     * Add a grade to a profile's class
     */
    @Transactional
    public Optional<Profiles> addGrade(Long profileId, String className, String grade) {
        return profileRepository.findById(profileId)
                .map(profile -> {
                    profile.getGradesFromClasses().put(className, grade);
                    return profileRepository.save(profile);
                });
    }
}