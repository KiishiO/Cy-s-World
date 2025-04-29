package onetoone.StudyTable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import onetoone.Persons.Person;
import onetoone.Persons.PersonRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sTables")
@Tag(name = "StudyTables", description = "APIs for managing study groups and tables")
public class StudyTableController {

    private final StudyTableRepository studyTableRepository;
    private final PersonRepository personRepository;

    public StudyTableController(StudyTableRepository studyTableRepository, PersonRepository personRepository) {
        this.personRepository = personRepository;
        this.studyTableRepository = studyTableRepository;
    }

    // Create a study table entry with JSON
    @Operation(summary = "Create a study table", description = "Creates a new study table group with one leader and up to 3 additional participants")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Study table successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Creator or participant not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/create")
    public ResponseEntity<?> createStudyTableJson(@RequestBody Map<String, Object> requestData) {
        try {
            // Get the creator's ID
            Long creatorId = ((Number) requestData.get("PersonId")).longValue();

            // Get the list of participant IDs (up to 3 additional members)
            @SuppressWarnings("unchecked")
            List<Number> participantIdNumbers = (List<Number>) requestData.get("FriendIds");

            if (participantIdNumbers == null) {
                participantIdNumbers = new ArrayList<>();
            }

            // Convert to List<Long>
            List<Long> participantIds = participantIdNumbers.stream()
                    .map(Number::longValue)
                    .collect(Collectors.toList());

            // Validate creator ID
            if (creatorId == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Creator ID is required"));
            }

            // Check if we're not exceeding 4 total participants (1 creator + up to 3 others)
            if (participantIds.size() > 3) {
                return ResponseEntity.badRequest().body(
                        createErrorResponse("Study table cannot have more than 4 participants (including creator)")
                );
            }

            // Add creator to avoid self-inclusion in participants
            if (participantIds.contains(creatorId)) {
                return ResponseEntity.badRequest().body(
                        createErrorResponse("TableLeader should not be included in participant list")
                );
            }

            // Check for creator existence
            Optional<Person> creatorOpt = personRepository.findById(creatorId);
            if (creatorOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Creator not found with ID: " + creatorId));
            }

            // Check for duplicate members
            Set<Long> uniqueIds = new HashSet<>(participantIds);
            if (uniqueIds.size() < participantIds.size()) {
                return ResponseEntity.badRequest().body(
                        createErrorResponse("Duplicate participant IDs are not allowed")
                );
            }

            // Fetch all participants and validate they exist
            List<Person> participants = new ArrayList<>();
            for (Long participantId : participantIds) {
                Optional<Person> participantOpt = personRepository.findById(participantId);
                if (participantOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(createErrorResponse("Participant not found with ID: " + participantId));
                }
                participants.add(participantOpt.get());
            }

            // Create and save the study table with all participants
            return createAndSaveStudyTableGroup(creatorOpt.get(), participants);
        } catch (ClassCastException e) {
            return ResponseEntity.badRequest().body(
                    createErrorResponse("Invalid format: participantIds must be an array of numbers")
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error creating study table: " + e.getMessage()));
        }
    }

    // Helper method to create and save study table
    private ResponseEntity<?> createAndSaveStudyTableGroup(Person creator, List<Person> participants) {
        StudyTable studyTable = new StudyTable();
        studyTable.setPerson(creator);
        studyTable.setFriend(participants);
        studyTable.setStatus(StudyTable.Status.PENDING);

        StudyTable savedStudyTable = studyTableRepository.save(studyTable);

        return ResponseEntity.ok(createStudyTableGroupResponse(savedStudyTable));
    }

    // Get ALL study tables
    @Operation(summary = "Get all study tables", description = "Retrieves all study tables in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all study tables"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/all")
    public ResponseEntity<?> getAllStudyTables() {
        try {
            List<StudyTable> studyTables = studyTableRepository.findAll();
            List<Map<String, Object>> responseList = new ArrayList<>();

            for (StudyTable studyTable : studyTables) {
                responseList.add(createStudyTableGroupResponse(studyTable));
            }

            return ResponseEntity.ok(responseList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving study tables: " + e.getMessage()));
        }
    }

    // Get study tables by person ID
    @Operation(summary = "Get study tables by person", description = "Retrieves all study tables where the specified person is the leader")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved study tables"),
            @ApiResponse(responseCode = "404", description = "Person not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/person/{personId}")
    public ResponseEntity<?> getStudyTablesByPerson(@PathVariable Long personId) {
        try {
            Optional<Person> personOpt = personRepository.findById(personId);
            if (personOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Person not found with ID: " + personId));
            }

            List<StudyTable> studyTables = studyTableRepository.findByPerson(personOpt.get());
            List<Map<String, Object>> responseList = new ArrayList<>();

            for (StudyTable studyTable : studyTables) {
                responseList.add(createStudyTableGroupResponse(studyTable));
            }

            return ResponseEntity.ok(responseList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving study tables: " + e.getMessage()));
        }
    }

    // Get study tables where person is friend
    @Operation(summary = "Get study tables as friend", description = "Retrieves all study tables where the specified person is a participant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved study tables"),
            @ApiResponse(responseCode = "404", description = "Person not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/friend/{friendId}")
    public ResponseEntity<?> getStudyTablesAsFriend(@PathVariable Long friendId) {
        try {
            Optional<Person> friendOpt = personRepository.findById(friendId);
            if (friendOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Person not found with ID: " + friendId));
            }

            List<StudyTable> studyTables = studyTableRepository.findByFriend(friendOpt.get());
            List<Map<String, Object>> responseList = new ArrayList<>();

            for (StudyTable studyTable : studyTables) {
                responseList.add(createStudyTableGroupResponse(studyTable));
            }

            return ResponseEntity.ok(responseList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving study tables: " + e.getMessage()));
        }
    }

    // Update study table status (accept or reject)
    @Operation(summary = "Respond to study table invitation", description = "Updates the status of a study table invitation (accept or reject)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated study table status"),
            @ApiResponse(responseCode = "400", description = "Invalid status value"),
            @ApiResponse(responseCode = "404", description = "Study table not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/respond")
    public ResponseEntity<?> respondToStudyTable(@RequestBody Map<String, Object> requestData) {
        try {
            Long studyTableId = Long.valueOf(requestData.get("studyTableId").toString());
            String status = requestData.get("status").toString();

            Optional<StudyTable> studyTableOpt = studyTableRepository.findById(studyTableId);

            if (studyTableOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Study table not found with ID: " + studyTableId));
            }

            StudyTable studyTable = studyTableOpt.get();

            try {
                StudyTable.Status newStatus = StudyTable.Status.valueOf(status.toUpperCase());
                studyTable.setStatus(newStatus);

                StudyTable savedStudyTable = studyTableRepository.save(studyTable);
                Map<String, Object> response = createStudyTableGroupResponse(savedStudyTable);

                if (newStatus == StudyTable.Status.ACCEPTED) {
                    response.put("message", "Study table request accepted");
                } else if (newStatus == StudyTable.Status.REJECTED) {
                    response.put("message", "Study table request rejected");
                }

                return ResponseEntity.ok(response);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Invalid status. Must be 'PENDING', 'ACCEPTED', or 'REJECTED'"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error updating study table: " + e.getMessage()));
        }
    }

    // Delete a study table
    @Operation(summary = "Delete a study table", description = "Removes a study table from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Study table successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Study table not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/delete/{studyTableId}")
    public ResponseEntity<?> deleteStudyTable(@PathVariable Long studyTableId) {
        try {
            Optional<StudyTable> studyTableOpt = studyTableRepository.findById(studyTableId);
            if (studyTableOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Study table not found with ID: " + studyTableId));
            }

            // Return the study table info before deleting
            Map<String, Object> response = createStudyTableGroupResponse(studyTableOpt.get());
            response.put("message", "Study table successfully deleted");

            studyTableRepository.deleteById(studyTableId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error deleting study table: " + e.getMessage()));
        }
    }

    // Get specific study table by ID
    @Operation(summary = "Get study table by ID", description = "Retrieves a specific study table by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved study table"),
            @ApiResponse(responseCode = "404", description = "Study table not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{studyTableId}")
    public ResponseEntity<?> getStudyTableById(@PathVariable Long studyTableId) {
        try {
            Optional<StudyTable> studyTableOpt = studyTableRepository.findById(studyTableId);
            if (studyTableOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Study table not found with ID: " + studyTableId));
            }

            return ResponseEntity.ok(createStudyTableGroupResponse(studyTableOpt.get()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving study table: " + e.getMessage()));
        }
    }

    // Helper method to create standardized study table response
    private Map<String, Object> createStudyTableGroupResponse(StudyTable studyTable) {
        Map<String, Object> response = new HashMap<>();
        response.put("studyTableId", studyTable.getId());
        response.put("status", studyTable.getStatus().toString());

        // Add creator details
        Map<String, Object> creatorDetails = new HashMap<>();
        creatorDetails.put("id", studyTable.getPerson().getId());
        creatorDetails.put("name", studyTable.getPerson().getName());
        response.put("TableLeader", creatorDetails);

        // Add all participants
        List<Map<String, Object>> participantsDetails = new ArrayList<>();
        for (Person participant : studyTable.getFriend()) {
            Map<String, Object> participantDetail = new HashMap<>();
            participantDetail.put("id", participant.getId());
            participantDetail.put("name", participant.getName());
            participantsDetails.add(participantDetail);
        }
        response.put("friendGroup", participantsDetails);
        response.put("totalMembers", participantsDetails.size() + 1); // +1 for creator

        return response;
    }

    // Helper method to create error response
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }
}