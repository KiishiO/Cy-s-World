package onetoone.Profiles;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profiles, Long> {

    Optional<Profiles> findById(Long id);

    Profiles findByClasses(String userClasses);

    Profiles findBygradedClass(char gradesFromClasses);





}
