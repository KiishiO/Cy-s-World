package onetoone.TestingCenter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Sonia Patil
 */
@Repository
public interface TestingCenterRepository extends JpaRepository<TestingCenter, Integer> {

     //find testing center by name
    //List<TestingCenter> findByCenterName(String name);

    //find by location
    //List<TestingCenter> findByLocation(String location);

    //find by specfic exam
    //List<TestingCenter> findByExamInfo(String examInfo);


}
