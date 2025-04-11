package onetoone.BusSystem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface busRepository extends JpaRepository<Bus, Long> {
    Optional<Bus> findByBusName(String busName);
    Optional<Bus> findByBusNum(int busNum);
    List<Bus> findByCurrentStopLocation(String currentStopLocation);

    // For checking route stops
    @Query("SELECT b FROM Bus b WHERE :stopLocation MEMBER OF b.stopLocations")
    List<Bus> findByStopLocationInRoute(String stopLocation);

    // Still keeping this for compatibility with existing code
    default List<Bus> findByStopLocation(String stopLocation) {
        return findByCurrentStopLocation(stopLocation);
    }
}