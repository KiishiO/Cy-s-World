package onetoone.FriendRequest;

import onetoone.FriendRequest.FriendRequestRepository;
import onetoone.Persons.Person;
import onetoone.Persons.PersonRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;

@RestController
@Tag(name = "Friend Request Management API")
@RequestMapping("/FriendRequests")
public class FriendRequestController {

    private final FriendRequestRepository requestRepository;
    private final PersonRepository personRepository;

    public FriendRequestController(FriendRequestRepository requestRepository, PersonRepository personRepository) {
        this.requestRepository = requestRepository;
        this.personRepository = personRepository;
    }

    /**
     * Sends a friend request to another person.
     * @param requestData A map containing senderId and receiverId.
     * @return A response entity with the result of the friend request operation.
     */
    @PostMapping("/send")
    @Operation(summary = "Send a friend request", description = "Sends a friend request from one person to another")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend request sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or request"),
            @ApiResponse(responseCode = "404", description = "Sender or receiver not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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

    /**
     * Creates and saves the friend request.
     * @param sender The sender of the request.
     * @param receiver The receiver of the request.
     * @return A response entity with the saved friend request.
     */
    private ResponseEntity<?> createAndSaveFriendRequest(Person sender, Person receiver) {
        FriendRequest request = new FriendRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(FriendRequest.Status.PENDING);

        FriendRequest savedRequest = requestRepository.save(request);

        return ResponseEntity.ok(createFriendRequestResponse(savedRequest));
    }

    /**
     * Retrieves all friend requests in the system.
     * @return A response entity with a list of all friend requests.
     */
    @GetMapping("/all")
    @Operation(summary = "Get all friend requests", description = "Retrieves all friend requests in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of all friend requests"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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

    /**
     * Retrieves all friend requests sent by a specific sender.
     * @param senderId The ID of the sender.
     * @return A response entity with the list of sent friend requests.
     */
    @GetMapping("/sent/{senderId}")
    @Operation(summary = "Get sent friend requests", description = "Retrieves all friend requests sent by a specific sender")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of sent friend requests"),
            @ApiResponse(responseCode = "404", description = "Sender not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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

    /**
     * Retrieves all friend requests received by a specific receiver.
     * @param receiverId The ID of the receiver.
     * @return A response entity with the list of received friend requests.
     */
    @GetMapping("/received/{receiverId}")
    @Operation(summary = "Get received friend requests", description = "Retrieves all friend requests received by a specific receiver")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of received friend requests"),
            @ApiResponse(responseCode = "404", description = "Receiver not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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

    /**
     * Accepts or rejects a specific friend request.
     * @param requestData A map containing the requestId and the desired status (ACCEPTED or REJECTED).
     * @return A response entity with the result of the operation.
     */
    @PostMapping("/respond")
    @Operation(summary = "Respond to a friend request", description = "Accepts or rejects a friend request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend request responded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status provided"),
            @ApiResponse(responseCode = "404", description = "Friend request not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> respondToRequest(@RequestBody Map<String, Object> requestData) {
        try {
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
                sender.getFriends().add(receiver);
                receiver.getFriends().add(sender);
                personRepository.save(sender);
                personRepository.save(receiver);

                FriendRequest savedRequest = requestRepository.save(request);
                Map<String, Object> response = createFriendRequestResponse(savedRequest);
                response.put("message", "Friend request accepted. Users are now friends.");

                return ResponseEntity.ok(response);
            } else if ("REJECTED".equalsIgnoreCase(status)) {
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

    /**
     * Cancels a specific friend request.
     * @param requestId The ID of the request to be cancelled.
     * @return A response entity with the result of the cancellation.
     */
    @DeleteMapping("/cancel/{requestId}")
    @Operation(summary = "Cancel a friend request", description = "Cancels a specific friend request by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend request cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Friend request not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> cancelFriendRequest(@PathVariable Long requestId) {
        try {
            Optional<FriendRequest> requestOpt = requestRepository.findById(requestId);
            if (requestOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Friend request not found with ID: " + requestId));
            }

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

    /**
     * Retrieves the friends list for a specific person.
     * @param personId The ID of the person whose friends list is to be retrieved.
     * @return A response entity with the details of the person's friends.
     */
    @GetMapping("/friends/{personId}")
    @Operation(summary = "Get friends list", description = "Retrieves the friends list of a person")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friends list retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Person not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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

    /**
     * Helper method to create a standardized response for a friend request.
     * @param request The friend request object.
     * @return A map containing the details of the friend request.
     */
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

    /**
     * Helper method to create an error response.
     * @param message The error message to be included in the response.
     * @return A map containing the error message.
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }
}
