package onetoone.FriendRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findByReceiverIdAndStatus(Long receiverId, FriendRequest.Status status);
    List<FriendRequest> findBySenderId(Long senderId);
}
