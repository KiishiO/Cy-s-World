package onetoone.Signup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Sonia Patil
 */
public interface SignupRepository extends JpaRepository<Signup, Long>{
    Signup findById(int id);

    Signup deleteById(int id);
    //potentially add a find by person id.
    Signup findByEmailId(String emailId);
}
