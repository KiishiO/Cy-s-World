package onetoone.FriendRequest;
import onetoone.FriendRequest.FriendRequestRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/FriendRequests")
public class FriendRequestController {

    private final FriendRequestRepository requestRepository;

    public FriendRequestController(FriendRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    // Modified to accept JSON body
    @PostMapping("/send")
    public ResponseEntity<FriendRequest> sendRequest(@RequestBody FriendRequest requestData) {
        if(requestData.getSenderId().equals(requestData.getReceiverId())) {
            return ResponseEntity.badRequest().build(); // no self requests
        }

        FriendRequest request = FriendRequest.builder()
                .senderId(requestData.getSenderId())
                .receiverId(requestData.getReceiverId())
                .status(FriendRequest.Status.PENDING)
                .build();

        return ResponseEntity.ok(requestRepository.save(request));
    }

//    @GetMapping("/sent")
//    public ResponseEntity<List<FriendRequest>> getSentRequests(
//            @RequestParam(value = "senderId", required = true) Long senderId) {
//        try {
//            List<FriendRequest> requests = requestRepository.findBySenderId(senderId);
//            return ResponseEntity.ok(requests);
//        } catch (Exception e) {
//            // Log the exception
//            System.err.println("Error retrieving sent requests: " + e.getMessage());
//            return ResponseEntity.status(500).build();
//        }
//    }

    // You can also add a new endpoint to get ALL sent requests
    @GetMapping("/all-sent")
    public ResponseEntity<List<FriendRequest>> getAllSentRequests() {
        try {
            List<FriendRequest> requests = requestRepository.findAll();
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            System.err.println("Error retrieving all sent requests: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // Option 2: Use path variable instead of request parameter
    @GetMapping("/sent/{senderId}")
    public ResponseEntity<List<FriendRequest>> getSentRequestsById(@PathVariable Long senderId) {
        List<FriendRequest> requests = requestRepository.findBySenderId(senderId);
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/respond")
    public ResponseEntity<FriendRequest> respondToRequest(@RequestBody FriendRequest requestData) {
        Optional<FriendRequest> requestOptional = requestRepository.findById(requestData.getId());

        if(requestOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        FriendRequest request = requestOptional.get();

        try {
            request.setStatus(FriendRequest.Status.valueOf(requestData.getStatus().toString()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(requestRepository.save(request));
    }

    @DeleteMapping("/cancel/{requestId}")
    public ResponseEntity<Void> cancelFriendRequest(@PathVariable Long requestId) {
        if(!requestRepository.existsById(requestId)){
            return ResponseEntity.notFound().build();
        }
        requestRepository.deleteById(requestId);
        return ResponseEntity.noContent().build();
    }
}