package onetoone.BusSystem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface busRepository extends JpaRepository<Bus, String> {
    Optional<Bus> findByBusName(String busName);
    Optional<Bus> findByBusNum(int busNum);
    List<Bus> findByCurrentStopLocation(String currentStopLocation);

    // For checking route stops
    @Query("SELECT b FROM Bus b WHERE :stopLocation MEMBER OF b.stopLocations")
    List<Bus> findByStopLocationInRoute(String stopLocation);

    // For compatibility with existing code
    default List<Bus> findByStopLocation(String stopLocation) {
        return findByCurrentStopLocation(stopLocation);
    }

    // GTFS integration queries
    List<Bus> findByRouteId(String routeId);
    Optional<Bus> findByVehicleId(String vehicleId);
    Optional<Bus> findByTripId(String tripId);

    // Spatial queries
    @Query("SELECT b FROM Bus b ORDER BY (b.latitude - :lat) * (b.latitude - :lat) + " +
            "(b.longitude - :lng) * (b.longitude - :lng) ASC")
    List<Bus> findNearestBuses(double lat, double lng);

    // Activity monitoring
    @Query("SELECT b FROM Bus b WHERE b.speed > 0")
    List<Bus> findActiveBuses();
}