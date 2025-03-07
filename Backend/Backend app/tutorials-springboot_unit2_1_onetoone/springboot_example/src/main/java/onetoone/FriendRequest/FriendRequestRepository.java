package onetoone.FriendRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import onetoone.Persons.Person;
import java.util.List;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findBySender(Person sender);
    List<FriendRequest> findByReceiver(Person receiver);

    // You may also want to keep these methods if you need them somewhere
    List<FriendRequest> findBySenderId(Long senderId);
    List<FriendRequest> findByReceiverId(Long receiverId);
}