package onetoone.BusSystem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface busRepository extends JpaRepository<Bus, Long> {
Optional<Bus> findByBusName(String busName);
Optional<Bus> findByBusNum(int busNum);
List<Bus> findByStopLocation(String stopLocation);

}
