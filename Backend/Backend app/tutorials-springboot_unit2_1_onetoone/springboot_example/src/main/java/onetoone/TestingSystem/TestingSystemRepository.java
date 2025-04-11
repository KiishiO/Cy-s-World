package onetoone.TestingSystem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestingSystemRepository extends JpaRepository<TestingSystem, Integer> {

    List<TestingSystem> findByLocationContainingIgnoreCase(String location);
}
