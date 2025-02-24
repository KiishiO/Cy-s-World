package onetoone.Signup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Vivek Bengre
 * 
 */ 

public interface SignupRepository extends JpaRepository<Signup, Long> {
    Signup findById(int id);

    @Transactional
    void deleteById(int id);
}
