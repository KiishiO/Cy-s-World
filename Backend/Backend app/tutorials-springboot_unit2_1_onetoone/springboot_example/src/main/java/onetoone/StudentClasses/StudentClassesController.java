package onetoone.StudentClasses;

import onetoone.Persons.Person;
import onetoone.Persons.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/classes")
public class StudentClassesController {

    @Autowired
    private StudentClassesRepository classesRepository;

    @Autowired
    private PersonRepository personRepository;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    // GET all classes
    @GetMapping
    public List<StudentClasses> getAllClasses() {
        return classesRepository.findAll();
    }

    // GET a specific class by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getClassById(@PathVariable int id) {
        StudentClasses studentClass = classesRepository.findById(id);
        if (studentClass == null) {
            return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(studentClass, HttpStatus.OK);
    }

    // GET all students in a class
    @GetMapping("/{id}/students")
    public ResponseEntity<?> getStudentsInClass(@PathVariable int id) {
        StudentClasses studentClass = classesRepository.findById(id);
        if (studentClass == null) {
            return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(studentClass.getStudents(), HttpStatus.OK);
    }

    // GET all classes for a specific student
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getClassesForStudent(@PathVariable int studentId) {
        Person student = personRepository.findById(studentId);
        if (student == null) {
            return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
        }
        List<StudentClasses> classes = classesRepository.findClassesByStudentId(studentId);
        return new ResponseEntity<>(classes, HttpStatus.OK);
    }

    // GET all classes taught by a specific teacher
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<?> getClassesByTeacher(@PathVariable int teacherId) {
        Person teacher = personRepository.findById(teacherId);
        if (teacher == null) {
            return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
        }
        List<StudentClasses> classes = classesRepository.findClassesByTeacherId(teacherId);
        return new ResponseEntity<>(classes, HttpStatus.OK);
    }

    // POST a new class
    @PostMapping
    public ResponseEntity<?> createClass(@RequestBody StudentClasses studentClass) {
        try {
            // Verify teacher exists
            if (studentClass.getTeacher() != null) {
                Person teacher = personRepository.findById(studentClass.getTeacher().getId());
                if (teacher == null) {
                    return new ResponseEntity<>("{\"message\":\"Teacher not found\"}", HttpStatus.BAD_REQUEST);
                }
                studentClass.setTeacher(teacher);

                // Check if class with same name and teacher already exists
                StudentClasses existingClass = classesRepository.findByClassNameAndTeacherId(
                        studentClass.getClassName(), teacher.getId());

                if (existingClass != null) {
                    return new ResponseEntity<>("{\"message\":\"A class with this name and teacher already exists\"}",
                            HttpStatus.CONFLICT);
                }

                StudentClasses existingClassName = classesRepository.findByClassName(
                        studentClass.getClassName());

                if(existingClassName != null){
                    return new ResponseEntity<>("{\"message\":\"This class name is already taken\"}",
                            HttpStatus.CONFLICT);
                }

            }

            classesRepository.save(studentClass);
            return new ResponseEntity<>(studentClass, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(failure, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // PUT/update an existing class
    @PutMapping("/{id}")
    public ResponseEntity<?> updateClass(@PathVariable int id, @RequestBody StudentClasses studentClass) {
        StudentClasses existingClass = classesRepository.findById(id);
        if (existingClass == null) {
            return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
        }

        // Verify teacher exists if provided
        if (studentClass.getTeacher() != null) {
            Person teacher = personRepository.findById(studentClass.getTeacher().getId());
            if (teacher == null) {
                return new ResponseEntity<>("{\"message\":\"Teacher not found\"}", HttpStatus.BAD_REQUEST);
            }
            studentClass.setTeacher(teacher);
        }

        // Ensure ID is preserved
        studentClass.setId(id);

        // Preserve existing students if not provided in request
        if (studentClass.getStudents() == null || studentClass.getStudents().isEmpty()) {
            studentClass.setStudents(existingClass.getStudents());
        }

        classesRepository.save(studentClass);
        return new ResponseEntity<>(classesRepository.findById(id), HttpStatus.OK);
    }

    // DELETE a class
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClass(@PathVariable int id) {
        try{
        StudentClasses existingClass = classesRepository.findById(id);
        if (existingClass == null) {
            return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
        }

        existingClass.setStudents(new HashSet<>());
        classesRepository.save(existingClass);

        // Then delete the class
        classesRepository.deleteById(id);
        return new ResponseEntity<>(success, HttpStatus.OK);
    } catch(Exception e) {
        e.printStackTrace(); // Log the full error
        return new ResponseEntity<>("{\"message\":\"" + e.getMessage() + "\"}",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}


    // ADD a student to a class
    @PostMapping("/{classId}/students/{studentId}")
    public ResponseEntity<?> addStudentToClass(@PathVariable int classId, @PathVariable int studentId) {
        StudentClasses studentClass = classesRepository.findById(classId);
        Person student = personRepository.findById(studentId);

        if (studentClass == null || student == null) {
            return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
        }


        studentClass.addStudent(student);
        classesRepository.save(studentClass);

        return new ResponseEntity<>(success, HttpStatus.OK);
    }

    // REMOVE a student from a class
    @DeleteMapping("/{classId}/students/{studentId}")
    public ResponseEntity<?> removeStudentFromClass(@PathVariable int classId, @PathVariable int studentId) {
        StudentClasses studentClass = classesRepository.findById(classId);
        Person student = personRepository.findById(studentId);

        if (studentClass == null || student == null) {
            return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
        }

        studentClass.removeStudent(student);
        classesRepository.save(studentClass);

        return new ResponseEntity<>(success, HttpStatus.OK);
    }

    // GET dashboard summary for admin
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardSummary() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalClasses", classesRepository.count());
        dashboard.put("classes", classesRepository.findAll());

        return new ResponseEntity<>(dashboard, HttpStatus.OK);
    }
}