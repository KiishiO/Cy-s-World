package onetoone.Signup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Vivek Bengre
 * 
 */ 
@Repository
public interface SignupRepository extends JpaRepository<Signup, Long> {
    Signup findById(int id);

    @Transactional
    void deleteById(int id);
}
