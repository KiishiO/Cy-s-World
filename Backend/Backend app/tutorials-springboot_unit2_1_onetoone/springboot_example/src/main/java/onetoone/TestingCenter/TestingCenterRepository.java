package onetoone.TestingCenter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Sonia Patil
 */@Repository
public interface TestingCenterRepository extends JpaRepository<TestingCenter, Integer> {

     //find testing center by name
    List<TestingCenter> findByTestingCenterName(String name);

    //find by location
    List<TestingCenter> findByTestingCenterLocation(String location);

    //find by specfic exam
    List<TestingCenter> findByExam(String examInfo);

}
