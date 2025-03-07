package onetoone.FriendRequest;
import onetoone.FriendRequest.FriendRequestRepository;
import onetoone.Persons.Person;
import onetoone.Persons.PersonRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.*;

@RestController
@RequestMapping("/FriendRequests")
public class FriendRequestController {

    private final FriendRequestRepository requestRepository;
    private final PersonRepository personRepository;

    public FriendRequestController(FriendRequestRepository requestRepository, PersonRepository personRepository) {
        this.requestRepository = requestRepository;
        this.personRepository = personRepository;
    }

    // Send a friend request with JSON
    @PostMapping("/send")
    public ResponseEntity<?> sendRequestJson(@RequestBody Map<String, Long> requestData) {
        try {
            Long senderId = requestData.get("senderId");
            Long receiverId = requestData.get("receiverId");

            if (senderId == null || receiverId == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Sender ID and Receiver ID are required"));
            }

            if (senderId.equals(receiverId)) {
                return ResponseEntity.badRequest().body(createErrorResponse("Cannot send friend request to yourself"));
            }

            Optional<Person> senderOpt = personRepository.findById(senderId);
            Optional<Person> receiverOpt = personRepository.findById(receiverId);

            if (senderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Sender not found with ID: " + senderId));
            }

            if (receiverOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Receiver not found with ID: " + receiverId));
            }

            return createAndSaveFriendRequest(senderOpt.get(), receiverOpt.get());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error creating friend request: " + e.getMessage()));
        }
    }

    // Helper method to create and save friend request
    private ResponseEntity<?> createAndSaveFriendRequest(Person sender, Person receiver) {

        FriendRequest request = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequest.Status.PENDING)
                .build();

        FriendRequest savedRequest = requestRepository.save(request);

        return ResponseEntity.ok(createFriendRequestResponse(savedRequest));
    }

    // Get ALL friend requests
    @GetMapping("/all")
    public ResponseEntity<?> getAllRequests() {
        try {
            List<FriendRequest> requests = requestRepository.findAll();
            List<Map<String, Object>> responseList = new ArrayList<>();

            for (FriendRequest request : requests) {
                responseList.add(createFriendRequestResponse(request));
            }

            return ResponseEntity.ok(responseList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving friend requests: " + e.getMessage()));
        }
    }

    // Get sent requests by sender ID
    @GetMapping("/sent/{senderId}")
    public ResponseEntity<?> getSentRequests(@PathVariable Long senderId) {
        try {
            Optional<Person> senderOpt = personRepository.findById(senderId);
            if (senderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Person not found with ID: " + senderId));
            }

            List<FriendRequest> requests = requestRepository.findBySender(senderOpt.get());
            List<Map<String, Object>> responseList = new ArrayList<>();

            for (FriendRequest request : requests) {
                responseList.add(createFriendRequestResponse(request));
            }

            return ResponseEntity.ok(responseList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving sent requests: " + e.getMessage()));
        }
    }

    // Get received requests
    @GetMapping("/received/{receiverId}")
    public ResponseEntity<?> getReceivedRequests(@PathVariable Long receiverId) {
        try {
            Optional<Person> receiverOpt = personRepository.findById(receiverId);
            if (receiverOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Person not found with ID: " + receiverId));
            }

            List<FriendRequest> requests = requestRepository.findByReceiver(receiverOpt.get());
            List<Map<String, Object>> responseList = new ArrayList<>();

            for (FriendRequest request : requests) {
                responseList.add(createFriendRequestResponse(request));
            }

            return ResponseEntity.ok(responseList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving received requests: " + e.getMessage()));
        }
    }

    // Accept or reject friend request
    @PostMapping("/respond")
    public ResponseEntity<?> respondToRequest(@RequestBody Map<String, Object> requestData) {
        try {
            // Extract requestId and status from JSON
            Long requestId = Long.valueOf(requestData.get("requestId").toString());
            String status = requestData.get("status").toString();

            Optional<FriendRequest> requestOptional = requestRepository.findById(requestId);

            if (requestOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Friend request not found with ID: " + requestId));
            }

            FriendRequest request = requestOptional.get();
            Person sender = request.getSender();
            Person receiver = request.getReceiver();

            if ("ACCEPTED".equalsIgnoreCase(status)) {
                request.setStatus(FriendRequest.Status.ACCEPTED);

                // Add each person to the other's friend list
                sender.getFriends().add(receiver);
                receiver.getFriends().add(sender);

                // Save updated persons
                personRepository.save(sender);
                personRepository.save(receiver);

                FriendRequest savedRequest = requestRepository.save(request);
                Map<String, Object> response = createFriendRequestResponse(savedRequest);
                response.put("message", "Friend request accepted. Users are now friends.");
                return ResponseEntity.ok(response);
            } else if ("REJECTED".equalsIgnoreCase(status)) {
                // Delete the request from the database
                requestRepository.deleteById(requestId);
                return ResponseEntity.ok(createErrorResponse("Friend request rejected and removed."));
            } else {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Invalid status. Must be 'ACCEPTED' or 'REJECTED'"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error responding to friend request: " + e.getMessage()));
        }
    }
    
    // Cancel a friend request
    @DeleteMapping("/cancel/{requestId}")
    public ResponseEntity<?> cancelFriendRequest(@PathVariable Long requestId) {
        try {
            Optional<FriendRequest> requestOpt = requestRepository.findById(requestId);
            if (requestOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Friend request not found with ID: " + requestId));
            }

            // Return the request info before deleting
            Map<String, Object> response = createFriendRequestResponse(requestOpt.get());
            response.put("message", "Friend request successfully cancelled");

            requestRepository.deleteById(requestId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error cancelling friend request: " + e.getMessage()));
        }
    }

    // Get Friends List with details
    @GetMapping("/friends/{personId}")
    public ResponseEntity<?> getFriends(@PathVariable Long personId) {
        try {
            Optional<Person> personOpt = personRepository.findById(personId);
            if (personOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Person not found with ID: " + personId));
            }

            List<Person> friends = personOpt.get().getFriends();
            List<Map<String, Object>> friendsDetails = new ArrayList<>();

            for (Person friend : friends) {
                Map<String, Object> friendDetail = new HashMap<>();
                friendDetail.put("id", friend.getId());
                friendDetail.put("name", friend.getName());
                // Add other properties you want to include
                friendsDetails.add(friendDetail);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("personId", personId);
            response.put("personName", personOpt.get().getName());
            response.put("friendCount", friends.size());
            response.put("friends", friendsDetails);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving friends list: " + e.getMessage()));
        }
    }

    // Helper method to create standardized friend request response
    private Map<String, Object> createFriendRequestResponse(FriendRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("requestId", request.getId());
        response.put("status", request.getStatus().toString());

        // Add sender details
        Map<String, Object> senderDetails = new HashMap<>();
        senderDetails.put("id", request.getSender().getId());
        senderDetails.put("name", request.getSender().getName());
        response.put("sender", senderDetails);

        // Add receiver details
        Map<String, Object> receiverDetails = new HashMap<>();
        receiverDetails.put("id", request.getReceiver().getId());
        receiverDetails.put("name", request.getReceiver().getName());
        response.put("receiver", receiverDetails);

        return response;
    }

    // Helper method to create error response
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }
}