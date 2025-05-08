package onetoone.TestingCenter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @Operation(summary = "Get all exams", description = "Retrieve a list of all exams")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of exams", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExamInfo.class)))
    @GetMapping("/exams")
    public ResponseEntity<List<ExamInfo>> getAllExams() {
        return ResponseEntity.ok(examInfoRepo.findAll());
    }
    @Operation(summary = "Get all students", description = "Retrieve a list of all students")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of students", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Person.class)))
    @GetMapping("/students")
    public ResponseEntity<List<Person>> getAllStudents() {
        return ResponseEntity.ok(personRepo.findAll());
    }
    @Operation(summary = "Get all testing centers", description = "Retrieve a list of all testing centers")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of testing centers", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TestingCenter.class)))
    @GetMapping("/testing-centers")
    public ResponseEntity<List<TestingCenter>> getAllTestingCenters() {
        return ResponseEntity.ok(testingCenterRepo.findAll());
    }

    // === Exam Operations ===
    //create an exam - this one works
    @Operation(summary = "create a new exam and add to testing center")
    @ApiResponse(responseCode = "201", description = "sucessfully created an exam and added to testing center")
    @PostMapping("/{id}/exam")
    public ResponseEntity<ExamInfo> createNewExam(@PathVariable int id, @RequestBody ExamInfo examInfo){
        return testingCenterRepo.findById(id)
                .map(testingcenter -> {
                    examInfo.setTestingCenter(testingcenter);
                    testingcenter.getExamInfo2().add(examInfo);
                    testingCenterRepo.save(testingcenter);
                    return new ResponseEntity<>(examInfo, HttpStatus.CREATED);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @Operation(summary = "Add testing centers to an exam", description = "Assign testing centers to a specific exam")
    @ApiResponse(responseCode = "200", description = "Successfully added testing centers to exam", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExamInfo.class)))
    @ApiResponse(responseCode = "404", description = "Exam not found", content = @Content(mediaType = "application/json"))
    @PostMapping("/exams/{examId}/add-testing-centers")
    public ResponseEntity<ExamInfo> addTestingCentersToExam(@PathVariable int examId, @RequestBody List<Integer> testingCenterIds) {
        ExamInfo examInfo = examInfoRepo.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        List<TestingCenter> testingCenters = testingCenterRepo.findAllById(testingCenterIds);
        examInfo.getTestingCenters().addAll(testingCenters);
        return new ResponseEntity<>(examInfoRepo.save(examInfo), HttpStatus.OK);
    }

    @Operation(summary = "Get students for a specific exam", description = "Retrieve a list of students signed up for a particular exam")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of students for exam", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Person.class)))
    @ApiResponse(responseCode = "404", description = "Exam not found", content = @Content(mediaType = "application/json"))
    @GetMapping("/exams/{examId}/students")
    public ResponseEntity<List<Person>> getStudentsForExam(@PathVariable int examId) {
        ExamInfo exam = examInfoRepo.findById(examId).orElse(null);
        if (exam == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(exam.getPersons());
    }

    @Operation(summary = "Get testing centers for a specific exam", description = "Retrieve a list of testing centers assigned to a specific exam")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of testing centers for exam", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TestingCenter.class)))
    @ApiResponse(responseCode = "404", description = "Exam not found", content = @Content(mediaType = "application/json"))
    @GetMapping("/exams/{examId}/testing-centers")
    public ResponseEntity<List<TestingCenter>> getTestingCentersForExam(@PathVariable int examId) {
        ExamInfo examInfo = examInfoRepo.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        return new ResponseEntity<>(examInfo.getTestingCenters(), HttpStatus.OK);
    }

    @Operation(summary = "Update an existing exam", description = "Update the details of an existing exam")
    @ApiResponse(responseCode = "200", description = "Exam updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExamInfo.class)))
    @ApiResponse(responseCode = "404", description = "Exam not found", content = @Content(mediaType = "application/json"))
    @PutMapping("/exams/{id}")
    public ResponseEntity<ExamInfo> updateExam(@PathVariable int id, @RequestBody ExamInfo updatedExam) {
        return examInfoRepo.findById(id)
                .map(exam -> {
                    exam.setExamName(updatedExam.getExamName());
                    exam.setExamDescription(updatedExam.getExamDescription());
                    return new ResponseEntity<>(examInfoRepo.save(exam), HttpStatus.OK);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete an exam", description = "Delete a specific exam")
    @ApiResponse(responseCode = "200", description = "Exam deleted successfully", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "404", description = "Exam not found", content = @Content(mediaType = "application/json"))
    @DeleteMapping("/exams/{id}")
    public ResponseEntity<Void> deleteExam(@PathVariable int id) {
        return examInfoRepo.findById(id)
                .map(exam -> {
                    exam.getTestingCenters().forEach(center -> center.getExamInfo2().remove(exam));
                    examInfoRepo.delete(exam);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete a testing center", description = "Delete a specific testing center")
    @ApiResponse(responseCode = "200", description = "Testing center deleted successfully", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "404", description = "Testing center not found", content = @Content(mediaType = "application/json"))
    @DeleteMapping("/testingcenters/{id}")
    public ResponseEntity<Void> deleteTestingCenter(@PathVariable int id) {
        return testingCenterRepo.findById(id)
                .map(tc -> {
                    testingCenterRepo.delete(tc);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // === Student-Exam Operations ===

    @Operation(summary = "Sign up a student for an exam", description = "Allow a student to sign up for an exam")
    @ApiResponse(responseCode = "200", description = "Student signed up for exam", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "404", description = "Student or Exam not found", content = @Content(mediaType = "application/json"))
    @PostMapping("/students/{studentId}/signup-exam/{examId}")
    public ResponseEntity<?> signUpForExam(@PathVariable int studentId, @PathVariable int examId) {
        Person student = personRepo.findById(studentId);
        ExamInfo exam = examInfoRepo.findById(examId).orElse(null);

        if (student == null || exam == null) return ResponseEntity.notFound().build();

        exam.getPersons().add(student);
        examInfoRepo.save(exam);

        return ResponseEntity.ok("Student signed up for exam.");
    }

    @Operation(summary = "Get all exams for a student", description = "Retrieve a list of all exams a student is enrolled in")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of exams for student", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExamInfo.class)))
    @ApiResponse(responseCode = "404", description = "Student not found", content = @Content(mediaType = "application/json"))
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

    @Operation(summary = "Get all exams for a testing center", description = "Retrieve a list of exams scheduled at a specific testing center")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved exams for testing center", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExamInfo.class)))
    @ApiResponse(responseCode = "404", description = "Testing center not found", content = @Content(mediaType = "application/json"))
    @GetMapping("/testing-centers/{testingCenterId}/exams")
    public ResponseEntity<List<ExamInfo>> getExamsForTestingCenter(@PathVariable int testingCenterId) {
        TestingCenter tc = testingCenterRepo.findById(testingCenterId)
                .orElseThrow(() -> new RuntimeException("Testing center not found"));
        return new ResponseEntity<>(tc.getExamInfo2(), HttpStatus.OK);
    }

    @Operation(summary = "Remove a student from an exam", description = "Remove a student from an exam")
    @ApiResponse(responseCode = "200", description = "Successfully removed student from exam", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "404", description = "Student or Exam not found", content = @Content(mediaType = "application/json"))
    @DeleteMapping("/students/{studentId}/exam/{examId}")
    public ResponseEntity<String> removeStudentFromExam(@PathVariable int studentId, @PathVariable int examId) {
        Person student = personRepo.findById(studentId);
        ExamInfo exam = examInfoRepo.findById(examId).orElse(null);

        if (student == null || exam == null) return ResponseEntity.notFound().build();

        exam.getPersons().remove(student);
        examInfoRepo.save(exam);
        return ResponseEntity.ok("Student removed from exam.");
    }

    @Operation(summary = "Update a student's exam enrollment", description = "Move a student from one exam to another")
    @ApiResponse(responseCode = "200", description = "Successfully moved student to the new exam", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "404", description = "Student or Exam not found", content = @Content(mediaType = "application/json"))
    @PutMapping("/students/{studentId}/update-exam")
    public ResponseEntity<String> updateStudentExam(@PathVariable int studentId,
                                                    @RequestParam int oldExamId,
                                                    @RequestParam int newExamId) {
        Person student = personRepo.findById(studentId);
        ExamInfo oldExam = examInfoRepo.findById(oldExamId).orElse(null);
        ExamInfo newExam = examInfoRepo.findById(newExamId).orElse(null);

        if (student == null || oldExam == null || newExam == null) {
            return ResponseEntity.notFound().build();
        }

        oldExam.getPersons().remove(student);
        newExam.getPersons().add(student);

        examInfoRepo.save(oldExam);
        examInfoRepo.save(newExam);

        return ResponseEntity.ok("Student moved to new exam.");
    }
}

