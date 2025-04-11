package onetoone.TestingCenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exam-info")
public class ExamInfoController {

    @Autowired
    private ExamInfoRepository examInfoRepository;

    @Autowired
    private TestingCenterRepository testingCenterRepository;

    @GetMapping
    public ResponseEntity<List<ExamInfo>> getExams() {
        return ResponseEntity.ok(examInfoRepository.findAll());
    }

    @PostMapping("/create")
    public ResponseEntity<ExamInfo> createExam(@RequestBody ExamInfo examInfo) {
        ExamInfo savedExam = examInfoRepository.save(examInfo);
        return new ResponseEntity<>(savedExam, HttpStatus.CREATED);
    }

    // Add Exam to Testing Centers (many-to-many)
    @PostMapping("/{examId}/add-testing-centers")
    public ResponseEntity<ExamInfo> addTestingCentersToExam(@PathVariable int examId, @RequestBody List<Integer> testingCenterIds) {
        ExamInfo examInfo = examInfoRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        List<TestingCenter> testingCenters = testingCenterRepository.findAllById(testingCenterIds);
        //examInfo.getTestingCenters().addAll(testingCenters);

        ExamInfo updatedExamInfo = examInfoRepository.save(examInfo);

        return new ResponseEntity<>(updatedExamInfo, HttpStatus.OK);
    }

    // Get all exams for a testing center
    @GetMapping("/testing-center/{testingCenterId}")
    public ResponseEntity<List<ExamInfo>> getExamsForTestingCenter(@PathVariable int testingCenterId) {
        TestingCenter testingCenter = testingCenterRepository.findById(testingCenterId)
                .orElseThrow(() -> new RuntimeException("Testing center not found"));

        return new ResponseEntity<>(testingCenter.getExamInfo(), HttpStatus.OK);
    }

    // Get all testing centers for an exam
    @GetMapping("/{examId}/testing-centers")
    public ResponseEntity<List<TestingCenter>> getTestingCentersForExam(@PathVariable int examId) {
        ExamInfo examInfo = examInfoRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        return new ResponseEntity<>(examInfo.getTestingCenters(), HttpStatus.OK);
    }
}
