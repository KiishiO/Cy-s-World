package onetoone.TestingSystem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for TestingSystem entities
 */
@RestController
@RequestMapping("/testing-systems")
public class TestingSystemController {

    @Autowired
    private TestingSystemRepository testingSystemRepository;

    @Autowired
    private ExamRepository examRepository;

    // Get all testing systems
    @GetMapping
    public List<TestingSystem> getAllTestingSystems() {
        return testingSystemRepository.findAll();
    }

    // Get testing system by ID
    @GetMapping("/{id}")
    public ResponseEntity<TestingSystem> getTestingSystemById(@PathVariable int id) {
        Optional<TestingSystem> testingSystem = testingSystemRepository.findById(id);
        return testingSystem.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Create a new testing system
    @PostMapping("/new")
    public ResponseEntity<TestingSystem> createTestingSystem(@RequestBody TestingSystem testingSystem) {
        TestingSystem savedTestingSystem = testingSystemRepository.save(testingSystem);
        return new ResponseEntity<>(savedTestingSystem, HttpStatus.CREATED);
    }

    // Update testing system
    @PutMapping("/{id}")
    public ResponseEntity<TestingSystem> updateTestingSystem(@PathVariable int id, @RequestBody TestingSystem testingSystemDetails) {
        return testingSystemRepository.findById(id)
                .map(existingTestingSystem -> {
                    existingTestingSystem.setLocation(testingSystemDetails.getLocation());
                    TestingSystem updatedTestingSystem = testingSystemRepository.save(existingTestingSystem);
                    return ResponseEntity.ok(updatedTestingSystem);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete testing system
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestingSystem(@PathVariable int id) {
        return testingSystemRepository.findById(id)
                .map(testingSystem -> {
                    testingSystemRepository.delete(testingSystem);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Search testing systems by location
    @GetMapping("/search/location")
    public List<TestingSystem> searchTestingSystemsByLocation(@RequestParam String location) {
        return testingSystemRepository.findByLocationContainingIgnoreCase(location);
    }

    // Get all exams for a testing system
    @GetMapping("/{id}/exams")
    public ResponseEntity<List<Exam>> getExams(@PathVariable int id) {
        return testingSystemRepository.findById(id)
                .map(testingSystem -> ResponseEntity.ok(testingSystem.getExams().stream().toList()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Assign an exam to a testing system
    @PostMapping("/{systemId}/assign-exam/{examId}")
    public ResponseEntity<?> assignExam(@PathVariable int systemId, @PathVariable int examId) {
        Optional<TestingSystem> systemOpt = testingSystemRepository.findById(systemId);
        Optional<Exam> examOpt = examRepository.findById(examId);

        if (systemOpt.isPresent() && examOpt.isPresent()) {
            TestingSystem testingSystem = systemOpt.get();
            Exam exam = examOpt.get();

            // Update the relationship from both sides
            exam.getTestingSystem().add(testingSystem);
            examRepository.save(exam);

            return new ResponseEntity<>("Exam assigned successfully", HttpStatus.OK);
        }

        return ResponseEntity.notFound().build();
    }
}