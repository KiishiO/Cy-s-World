package onetoone.StudentClasses;

import onetoone.Persons.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentClassesRepository extends JpaRepository<StudentClasses, Integer> {

    StudentClasses findById(int id);

    void deleteById(int id);

    List<StudentClasses> findByTeacher(Person teacher);

    @Query("SELECT c FROM StudentClasses c JOIN c.students s WHERE s.id = :studentId")
    List<StudentClasses> findClassesByStudentId(@Param("studentId") int studentId);

    @Query("SELECT s FROM StudentClasses c JOIN c.students s WHERE c.id = :classId")
    List<Person> findStudentsByClassId(@Param("classId") int classId);

    @Query("SELECT c FROM StudentClasses c WHERE c.teacher.id = :teacherId")
    List<StudentClasses> findClassesByTeacherId(@Param("teacherId") int teacherId);
}