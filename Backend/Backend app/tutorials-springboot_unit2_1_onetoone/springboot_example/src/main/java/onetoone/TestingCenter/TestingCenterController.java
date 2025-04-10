package onetoone.TestingCenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * @Sonia Patil
 */
@RestController
@RequestMapping("/testingcenter")
public class TestingCenterController {

    @Autowired
    private TestingCenterRepository testingCenterRepository;

    //get all the testing centers
    @GetMapping
    public List<TestingCenter> getAllTestingCenters() { return testingCenterRepository.findAll(); }

    //get testing center by the id
    @GetMapping("/{id}")
    public ResponseEntity<TestingCenter> getTestingCenterByID(@PathVariable int id) {
        Optional<TestingCenter> testingCenter = testingCenterRepository.findById(id);
        return testingCenter.map(ResponseEntity::ok) //if there is a value, http status ok
            .orElseGet(() -> ResponseEntity.notFound().build()); //no value 404 message
    }

    // create a new testing center
    @PostMapping("/new")
    public ResponseEntity<TestingCenter> createTestingCenter(@RequestBody TestingCenter testingCenter) {
        TestingCenter savedTestingCenter = testingCenterRepository.save(testingCenter);
        return new ResponseEntity<>(savedTestingCenter, HttpStatus.CREATED);
    }

    //update a testing center
    @PutMapping("/{id}")
    public ResponseEntity<TestingCenter> updateTestingCenter(@PathVariable int id, @RequestBody TestingCenter testingCenter) {
        return testingCenterRepository.findById(id)
                .map(existingTestingCenter -> {
                    existingTestingCenter.setCenterName(testingCenter.getCenterName());
                    existingTestingCenter.setLocation(testingCenter.getLocation());
                    existingTestingCenter.setCenterDescription(testingCenter.getCenterDescription());
                    TestingCenter updatedTestingCenter = testingCenterRepository.save(existingTestingCenter);
                    return ResponseEntity.ok(updatedTestingCenter);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    //delete a testing center
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestingCenter(@PathVariable int id) {
        return testingCenterRepository.findById(id)
                .map(testingCenter -> {
                    testingCenterRepository.delete(testingCenter);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    //add an exam to a testing center
    @PostMapping("/{id}/exams")
    public ResponseEntity<ExamInfo> addExamInfo(@PathVariable int id, @RequestBody ExamInfo examInfo) {
        return testingCenterRepository.findById(id)
                .map(testingCenter -> {
                    examInfo.setExamName(examInfo.getExamName());
                    examInfo.setExamDescription(examInfo.getExamDescription());
                    examInfo.setTestingCenter(testingCenter);
                    return new ResponseEntity<>(examInfo, HttpStatus.CREATED);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    //get all the exams for a specific testing center
    @GetMapping("/{id}/exams")
    public ResponseEntity<List<ExamInfo>> getExamInfo(@PathVariable int id) {
        return testingCenterRepository.findById(id)
                .map(testingCenter -> ResponseEntity.ok(testingCenter.getExamInfo()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
