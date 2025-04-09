package onetoone.Bookstore;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Sonia Patil
 */
@Repository
public interface BookstoreRepository extends JpaRepository<Bookstore, Integer> {


}
