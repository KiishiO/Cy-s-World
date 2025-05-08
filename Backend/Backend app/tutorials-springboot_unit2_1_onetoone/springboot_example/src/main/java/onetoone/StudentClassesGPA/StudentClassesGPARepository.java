package onetoone.StudentClassesGPA;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentClassesGPARepository extends JpaRepository<StudentClassesGPA, Integer> {

    StudentClassesGPA findById(int id);

    List<StudentClassesGPA> findByStudentClassId(int classId);

    List<StudentClassesGPA> findByStudentId(int studentId);

    List<StudentClassesGPA> findByStudentClassIdAndStudentId(int classId, int studentId);

    @Query("SELECT gpa FROM StudentClassesGPA gpa WHERE gpa.studentClass.id = :classId AND gpa.student.id = :studentId AND gpa.assignmentName = :assignmentName")
    StudentClassesGPA findByClassStudentAndAssignment(int classId, int studentId, String assignmentName);

    @Query("SELECT SUM(gpa.grade * gpa.weightPercentage / 100) / SUM(gpa.weightPercentage / 100) FROM StudentClassesGPA gpa WHERE gpa.studentClass.id = :classId AND gpa.student.id = :studentId AND gpa.grade IS NOT NULL")
    Double calculateOverallGradeForStudent(int classId, int studentId);

    @Query("SELECT COUNT(gpa) FROM StudentClassesGPA gpa WHERE gpa.studentClass.id = :classId AND gpa.student.id = :studentId")
    Long countAssignmentsForStudent(int classId, int studentId);

    @Query("SELECT COUNT(gpa) FROM StudentClassesGPA gpa WHERE gpa.studentClass.id = :classId AND gpa.student.id = :studentId AND gpa.grade IS NOT NULL")
    Long countGradedAssignmentsForStudent(int classId, int studentId);

    @Query("SELECT AVG(gpa.grade) FROM StudentClassesGPA gpa WHERE gpa.studentClass.id = :classId AND gpa.grade IS NOT NULL")
    Double calculateClassAverage(int classId);
}