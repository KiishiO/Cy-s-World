package onetoone.BusSystem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;


public interface busRepository extends JpaRepository<Bus, Long> {



}
