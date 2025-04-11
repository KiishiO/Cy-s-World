package onetoone.TestingCenter;

import onetoone.Persons.Person;
import onetoone.Persons.PersonRepository;
import onetoone.TestingCenter.ExamInfo;
import onetoone.TestingCenter.ExamInfoRepository;
import onetoone.TestingCenter.TestingCenter;
import onetoone.TestingCenter.TestingCenterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class UnifiedController {

    @Autowired
    private PersonRepository personRepo;

    @Autowired
    private ExamInfoRepository examInfoRepo;

    @Autowired
    private TestingCenterRepository testingCenterRepo;

    // === Base Requests ===

    @GetMapping("/exams")
    public ResponseEntity<List<ExamInfo>> getAllExams() {
        return ResponseEntity.ok(examInfoRepo.findAll());
    }

    @GetMapping("/students")
    public ResponseEntity<List<Person>> getAllStudents() {
        return ResponseEntity.ok(personRepo.findAll());
    }

    @GetMapping("/testing-centers")
    public ResponseEntity<List<TestingCenter>> getAllTestingCenters() {
        return ResponseEntity.ok(testingCenterRepo.findAll());
    }

    // === Exam Operations ===

    @PostMapping("/exams")
    public ResponseEntity<ExamInfo> createExam(@RequestBody ExamInfo examInfo) {
        return new ResponseEntity<>(examInfoRepo.save(examInfo), HttpStatus.CREATED);
    }

    @PostMapping("/exams/{examId}/add-testing-centers")
    public ResponseEntity<ExamInfo> addTestingCentersToExam(@PathVariable int examId, @RequestBody List<Integer> testingCenterIds) {
        ExamInfo examInfo = examInfoRepo.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        List<TestingCenter> testingCenters = testingCenterRepo.findAllById(testingCenterIds);
        examInfo.getTestingCenters().addAll(testingCenters);
        return new ResponseEntity<>(examInfoRepo.save(examInfo), HttpStatus.OK);
    }

    @GetMapping("/exams/{examId}/students")
    public ResponseEntity<List<Person>> getStudentsForExam(@PathVariable int examId) {
        ExamInfo exam = examInfoRepo.findById(examId).orElse(null);
        if (exam == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(exam.getPersons());
    }

    @GetMapping("/exams/{examId}/testing-centers")
    public ResponseEntity<List<TestingCenter>> getTestingCentersForExam(@PathVariable int examId) {
        ExamInfo examInfo = examInfoRepo.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        return new ResponseEntity<>(examInfo.getTestingCenters(), HttpStatus.OK);
    }

    // === Student-Exam Operations ===

    @PostMapping("/students/{studentId}/signup-exam/{examId}")
    public ResponseEntity<?> signUpForExam(@PathVariable int studentId, @PathVariable int examId) {
        Person student = personRepo.findById(studentId);
        ExamInfo exam = examInfoRepo.findById(examId).orElse(null);

        if (student == null || exam == null) return ResponseEntity.notFound().build();

        exam.getPersons().add(student);
        examInfoRepo.save(exam);

        return ResponseEntity.ok("Student signed up for exam.");
    }

    @GetMapping("/students/{studentId}/exams")
    public ResponseEntity<List<ExamInfo>> getExamsForStudent(@PathVariable int studentId) {
        Person student = personRepo.findById(studentId);
        if (student == null) return ResponseEntity.notFound().build();

        List<ExamInfo> exams = new ArrayList<>();
        for (ExamInfo exam : examInfoRepo.findAll()) {
            if (exam.getPersons().contains(student)) {
                exams.add(exam);
            }
        }
        return ResponseEntity.ok(exams);
    }

    // === Testing Center Exam Lookup ===

    @GetMapping("/testing-centers/{testingCenterId}/exams")
    public ResponseEntity<List<ExamInfo>> getExamsForTestingCenter(@PathVariable int testingCenterId) {
        TestingCenter tc = testingCenterRepo.findById(testingCenterId)
                .orElseThrow(() -> new RuntimeException("Testing center not found"));
        return new ResponseEntity<>(tc.getExamInfo2(), HttpStatus.OK);
    }
}

