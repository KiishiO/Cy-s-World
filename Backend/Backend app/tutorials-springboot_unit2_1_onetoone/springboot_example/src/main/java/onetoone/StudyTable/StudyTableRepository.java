package onetoone.StudyTable;

import onetoone.Persons.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyTableRepository extends JpaRepository<StudyTable, Long> {

    List<StudyTable> findByPerson(Person person);
    List<StudyTable> findByFriend(Person friend);

    //Extra for help//
    List<StudyTable> findByPersonId(Long personId);
    List<StudyTable> findByFriendId(Long friendId);

}
