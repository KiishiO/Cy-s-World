package onetoone.CampusEvents;
//Author: Jayden Sorter
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a WebSocket server for handling real-time campus events communication.
 * Users connect to receive notifications about new events and can post events in real-time.
 *
 * This class is annotated with Spring's `@ServerEndpoint` and `@Component`
 * annotations, making it a WebSocket endpoint that can handle WebSocket
 * connections at the "/events/{username}" endpoint.
 *
 * Example URL: ws://localhost:8080/events/username
 *
 * The server provides functionality for broadcasting event notifications to all connected
 * users and allows users to post new events.
 */
@ServerEndpoint("/events/{username}")
@Component
public class CampusEventsWebSocket {

    private static CampusEventsRepository eventRepo;

    @Autowired
    public void setCampusEventsRepository(CampusEventsRepository repo){
        eventRepo = repo;

    }

    // Store all socket sessions and their corresponding username
    private static Map<Session, String> sessionUsernameMap = new ConcurrentHashMap<>();
    private static Map<String, Session> usernameSessionMap = new ConcurrentHashMap<>();

    // JSON object mapper
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // Register the JavaTimeModule to handle LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());

        // Disable timestamp serialization as arrays which can cause problems
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    // Server side logger
    private final Logger logger = LoggerFactory.getLogger(CampusEventsWebSocket.class);


    /**
     * This method is called when a new WebSocket connection is established.
     *
     * @param session represents the WebSocket session for the connected user.
     * @param username username specified in path parameter.
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) throws IOException {
        // Server side log
        logger.info("[onOpen] " + username);

        // Handle the case of a duplicate username
        if (usernameSessionMap.containsKey(username)) {
            session.getBasicRemote().sendText("Username already exists");
            session.close();
        } else {
            // Map current session with username
            sessionUsernameMap.put(session, username);

            // Map current username with session
            usernameSessionMap.put(username, session);

            // Send welcome message to the user joining
            sendMessageToUser(username, "Welcome to the Campus Events notification, " + username);

            // Send the campus events history to the new user
            sendMessageToUser(username, getEventsHistory());


            // Notify everyone about new user
            broadcast("User: " + username + " joined the Campus Events");
        }
    }

    /**
     * Handles incoming WebSocket messages from a client.
     * Messages can be new event posts or commands.
     *
     * @param session The WebSocket session representing the client's connection.
     * @param message The message received from the client.
     */
    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        // Get the username by session
        String username = sessionUsernameMap.get(session);

        // Server side log
        logger.info("[onMessage] " + username + ": " + message);

        // Check if the message appears to be JSON (starts with { and ends with })
        // This is a basic validation that can be enhanced for more accuracy
        if (message.trim().startsWith("{") && message.trim().endsWith("}")) {
            try {
                // First try to parse as a generic Map to validate JSON structure
                Map<String, Object> jsonMap = objectMapper.readValue(message, Map.class);
                logger.info("Successfully parsed as JSON structure: " + jsonMap);

                // Try to parse the incoming message as a CampusEvent
                CampusEvents event = objectMapper.readValue(message, CampusEvents.class);

                // Add the username as the creator of the event if not already set
                if (event.getCreator() == null || event.getCreator().isEmpty()) {
                    event.setCreator(username);
                }

                // Save the event to the database
                CampusEvents savedEvent = eventRepo.save(event);

                // Convert event back to JSON for broadcasting
                String eventJson = objectMapper.writeValueAsString(savedEvent);

                // Broadcast the new event to all connected users
                broadcast("NEW_EVENT: " + eventJson);

                logger.info("[Event Posted] by " + username + ": " + event.getTitle());
            } catch (IOException e) {
                // JSON parsing failed - this could be malformed JSON
                logger.error("Failed to parse JSON: " + e.getMessage(), e);
                sendMessageToUser(username, "Error: Invalid event format. Please check your JSON syntax." + e.getMessage());
            }
        } else if (message.startsWith("/")) {
            // Handle commands
            if (message.startsWith("/subscribe ")) {
                String category = message.substring(11);
                sendMessageToUser(username, "You've subscribed to events in category: " + category);
            } else if (message.startsWith("/help")) {
                sendMessageToUser(username, "Available commands: \n" +
                        "/help - Show this help message\n" +
                        "/list - Show all campus events\n" +
                        "/subscribe [category] - Subscribe to specific event category\n" +
                        "Or send a JSON event object to post a new event");
            } else if (message.startsWith("/list")) {
                sendMessageToUser(username, getEventsHistory());
            } else {
                sendMessageToUser(username, "Unknown command. Type /help for available commands.");
            }
        } else {
            // Handle as regular chat message
            saveChatMessage(username, message);
            broadcast(username + ": " + message);
        }
    }

    //Helper to save user messages
    private void saveChatMessage(String username, String message) {
        CampusEvents chatMessage = new CampusEvents();
        chatMessage.setTitle("Text Message");
        chatMessage.setDescription(message);
        chatMessage.setLocation("N/A"); // No location for chat messages
        chatMessage.setStartTime(LocalDateTime.now()); // Timestamp of message
        chatMessage.setCreator(username);
        chatMessage.setCategory("Chat Message");

        // Save the chat message to the eventRepo (database)
        eventRepo.save(chatMessage);
    }

    /**
     * Handles the closure of a WebSocket connection.
     *
     * @param session The WebSocket session that is being closed.
     */
    @OnClose
    public void onClose(Session session) throws IOException {
        // Get the username from session-username mapping
        String username = sessionUsernameMap.get(session);

        // Server side log
        logger.info("[onClose] " + username);

        // Remove user from memory mappings
        sessionUsernameMap.remove(session);
        usernameSessionMap.remove(username);

        // Notify other users
        broadcast(username + " disconnected from Campus Events");
    }

    /**
     * Handles WebSocket errors that occur during the connection.
     *
     * @param session   The WebSocket session where the error occurred.
     * @param throwable The Throwable representing the error condition.
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        // Get the username from session-username mapping
        String username = sessionUsernameMap.get(session);

        // Do error handling here
        logger.error("[onError] " + username + ": " + throwable.getMessage());

        // Remove session on error
        sessionUsernameMap.remove(session);
        if (username != null) {
            usernameSessionMap.remove(username);
        }
    }

    /**
     * Sends a message to a specific user.
     *
     * @param username The username of the recipient.
     * @param message  The message to be sent.
     */
    private void sendMessageToUser(String username, String message) {
        try {
            if (usernameSessionMap.containsKey(username)) {
                usernameSessionMap.get(username).getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            logger.error("[Send Message Exception] " + e.getMessage());
        }
    }

    /**
     * Broadcasts a message to all connected users.
     *
     * @param message The message to be broadcasted to all users.
     */
    private void broadcast(String message) {
        for (Session session : sessionUsernameMap.keySet()) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    logger.error("Error broadcasting message: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Public method to broadcast an event to all connected clients.
     * Can be called from other parts of the application.
     *
     * @param event The event to be broadcasted to all users.
     */
    public static void broadcastEvent(CampusEvents event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String broadcastMessage = "NEW_EVENT: " + eventJson;

            sessionUsernameMap.forEach((session, username) -> {
                try {
                    if (session.isOpen()) {
                        session.getBasicRemote().sendText(broadcastMessage);
                    }
                } catch (IOException e) {
                    // Log error but continue with other sessions
                    LoggerFactory.getLogger(CampusEventsWebSocket.class).error(
                            "[BroadcastEvent Exception] " + e.getMessage());
                }
            });
        } catch (IOException e) {
            LoggerFactory.getLogger(CampusEventsWebSocket.class).error(
                    "[BroadcastEvent Serialization Exception] " + e.getMessage());
        }
    }

    private String getEventsHistory() {
        List<CampusEvents> events = eventRepo.findAll();

        StringBuilder sb = new StringBuilder();
        sb.append("=== CAMPUS EVENTS & CHAT HISTORY ===\n\n");

        if (events != null && !events.isEmpty()) {
            for (CampusEvents event : events) {
                if ("Chat Message".equals(event.getCategory())) {
                    // Format as chat/text message
                    sb.append("💬 ").append(event.getCreator()).append(": ")
                            .append(event.getDescription())
                            .append(" [").append(formatTime(event.getStartTime())).append("]")
                            .append("\n\n");
                } else {
                    // Format as email/notification
                    sb.append("📢 NEW EVENT NOTIFICATION ").append("#").append(event.getId()).append("\n")
                            .append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                            .append("📌 Title: ").append(event.getTitle()).append("\n")
                            .append("📝 Description: ").append(event.getDescription()).append("\n")
                            .append("📍 Location: ").append(event.getLocation()).append("\n")
                            .append("🕒 Starts: ").append(formatDateTime(event.getStartTime())).append("\n")
                            .append("🕓 Ends: ").append(formatDateTime(event.getEndTime())).append("\n")
                            .append("👤 Posted by: ").append(event.getCreator()).append("\n")
                            .append("🏷️ Category: ").append(event.getCategory()).append("\n")
                            .append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                }
            }
        } else {
            sb.append("No events or messages have been posted yet.\n");
        }

        return sb.toString();
    }

    /**
     * Formats a LocalDateTime object to a user-friendly date and time string
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        // Format: "Monday, April 10, 2025 at 2:00 PM"
        return dateTime.format(java.time.format.DateTimeFormatter
                .ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a"));
    }

    /**
     * Formats a LocalDateTime object to show just the time for chat messages
     */
    private String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        // Format: "2:00 PM"
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"));
    }


}