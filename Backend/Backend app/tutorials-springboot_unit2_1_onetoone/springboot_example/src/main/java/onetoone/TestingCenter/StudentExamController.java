package onetoone.TestingCenter;

import onetoone.Persons.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import onetoone.Persons.PersonRepository;

import java.util.ArrayList;
import java.util.List;
@RestController
@RequestMapping("/students")
public class StudentExamController {

    @Autowired
    private PersonRepository personRepo;

    @Autowired
    private ExamInfoRepository examInfoRepo;

    // Sign a student up for an exam
    @PostMapping("/{studentId}/signup-exam/{examId}")
    public ResponseEntity<?> signUpForExam(@PathVariable int studentId, @PathVariable int examId) {
        Person student = personRepo.findById(studentId);
        ExamInfo exam = examInfoRepo.findById(examId).orElse(null);

        if (student == null || exam == null) {
            return ResponseEntity.notFound().build();
        }

        exam.getPersons().add(student);
        examInfoRepo.save(exam);

        return ResponseEntity.ok("Student signed up for exam.");
    }

    // Get all exams a student is taking
    @GetMapping("/{studentId}/exams")
    public ResponseEntity<List<ExamInfo>> getStudentExams(@PathVariable int studentId) {
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

    // Get all students taking a specific exam
    @GetMapping("/exam/{examId}/students")
    public ResponseEntity<List<Person>> getStudentsForExam(@PathVariable int examId) {
        ExamInfo exam = examInfoRepo.findById(examId).orElse(null);
        if (exam == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(exam.getPersons());
    }
}
