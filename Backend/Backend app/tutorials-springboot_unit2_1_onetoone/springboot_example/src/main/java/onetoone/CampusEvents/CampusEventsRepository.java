package onetoone.CampusEvents;

import org.aspectj.apache.bcel.generic.LOOKUPSWITCH;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface CampusEventsRepository extends JpaRepository<CampusEvents, Long>{
}
