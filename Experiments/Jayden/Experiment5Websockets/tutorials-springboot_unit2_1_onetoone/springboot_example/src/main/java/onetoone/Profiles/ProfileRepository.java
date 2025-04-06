package onetoone.Profiles;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profiles, Long> {

    Optional<Profiles> findById(Long id);

    /**
     * Find profiles by person id
     */
    List<Profiles> findByPersonId(Long personId);

    Profiles findByUserClasses(String userClasses);

//    Profiles findBygradedClass(char gradesFromClasses);





}
