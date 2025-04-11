package onetoone.TestingSystem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Exam entities
 */
@RestController
@RequestMapping("/exams")
public class ExamController {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private TestingSystemRepository testingSystemRepository;

    // Get all exams
    @GetMapping
    public List<Exam> getAllExams() {
        return examRepository.findAll();
    }

    // Get exam by ID
    @GetMapping("/{id}")
    public ResponseEntity<Exam> getExamById(@PathVariable int id) {
        Optional<Exam> exam = examRepository.findById(id);
        return exam.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Create a new exam
    @PostMapping("/new")
    public ResponseEntity<Exam> createExam(@RequestBody Exam exam) {
        Exam savedExam = examRepository.save(exam);
        return new ResponseEntity<>(savedExam, HttpStatus.CREATED);
    }

    // Update exam
    @PutMapping("/{id}")
    public ResponseEntity<Exam> updateExam(@PathVariable int id, @RequestBody Exam examDetails) {
        return examRepository.findById(id)
                .map(existingExam -> {
                    existingExam.setSubject(examDetails.getSubject());
                    Exam updatedExam = examRepository.save(existingExam);
                    return ResponseEntity.ok(updatedExam);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete exam
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExam(@PathVariable int id) {
        return examRepository.findById(id)
                .map(exam -> {
                    examRepository.delete(exam);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Search exams by subject
    @GetMapping("/search/subject")
    public List<Exam> searchExamsBySubject(@RequestParam String subject) {
        return examRepository.findBySubjectContainingIgnoreCase(subject);
    }

    // Assign testing system to an exam
    @PostMapping("/{examId}/assign-system/{systemId}")
    public ResponseEntity<?> assignTestingSystem(@PathVariable int examId, @PathVariable int systemId) {
        Optional<Exam> examOpt = examRepository.findById(examId);
        Optional<TestingSystem> systemOpt = testingSystemRepository.findById(systemId);

        if (examOpt.isPresent() && systemOpt.isPresent()) {
            Exam exam = examOpt.get();
            TestingSystem testingSystem = systemOpt.get();

            exam.getTestingSystem().add(testingSystem);
            examRepository.save(exam);

            return new ResponseEntity<>("Testing system assigned successfully", HttpStatus.OK);
        }

        return ResponseEntity.notFound().build();
    }

    // Get all testing systems for an exam
    @GetMapping("/{id}/testing-systems")
    public ResponseEntity<List<TestingSystem>> getTestingSystems(@PathVariable int id) {
        return examRepository.findById(id)
                .map(exam -> ResponseEntity.ok(exam.getTestingSystem().stream().toList()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}