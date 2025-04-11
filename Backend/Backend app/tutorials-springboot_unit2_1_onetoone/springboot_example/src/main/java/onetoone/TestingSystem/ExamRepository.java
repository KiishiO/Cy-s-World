package onetoone.TestingSystem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Integer> {
    List<Exam> findBySubjectContainingIgnoreCase(String subject);
}
