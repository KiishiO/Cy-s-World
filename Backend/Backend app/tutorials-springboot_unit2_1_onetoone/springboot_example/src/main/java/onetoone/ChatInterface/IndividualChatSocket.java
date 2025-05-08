package onetoone.ChatInterface;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * @author Sonia Patil
 */
@Controller
@ServerEndpoint(value = "/chat/1/{username}")
public class IndividualChatSocket {

    // cannot autowire static directly (instead we do it by the below
    // method
    private static MessageRepository msgRepo;

    /*
     * Grabs the MessageRepository singleton from the Spring Application
     * Context.  This works because of the @Controller annotation on this
     * class and because the variable is declared as static.
     * There are other ways to set this. However, this approach is
     * easiest.
     */
    @Autowired
    public void setMessageRepository(MessageRepository repo) {
        msgRepo = repo;  // we are setting the static variable
    }

    // Store all socket session and their corresponding username.
    private static Map<Session, String> sessionUsernameMap = new Hashtable<>();
    private static Map<String, Session> usernameSessionMap = new Hashtable<>();
    private static Map<String, String> userStatusMap = new Hashtable<>(); //Stores the user status
    private static Map<String, Message> messageIdMap = new Hashtable<>(); //Stores messages by ID for edit/delete

    private final Logger logger = LoggerFactory.getLogger(IndividualChatSocket.class);
//used to be ChatSocket.class
    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username)
            throws IOException {

        logger.info("Entered into Open");

        // store connecting user information
        sessionUsernameMap.put(session, username);
        usernameSessionMap.put(username, session);
        userStatusMap.put(username, "online"); //Default status is online

        //Send chat history to the newly connected user
        sendMessageToPArticularUser(username, getChatHistory());

        // broadcast that new user joined
        String message = "User:" + username + " has Joined the Chat [Online]";
        broadcast(message);
    }


    @OnMessage
    public void onMessage(Session session, String message) throws IOException {

        // Handle new messages
        logger.info("Entered into Message: Got Message:" + message);
        String username = sessionUsernameMap.get(session);

        //Handle status change
        if(message.startsWith("__status__")) {
            String status = message.split(" ")[1];
            if(status.equals("active") || status.equals("online") || status.equals("inactive")) {
                userStatusMap.put(username, status);
                broadcast(username + " is now " + status);
            }
            return;
        }

        //editing a message
        // Handle editing message
        if (message.startsWith("__edit__")) {
            // Format: __edit__ messageId newContent
            String[] parts = message.split(" ", 3);
            if (parts.length >= 3) {
                String messageId = parts[1];
                String newContent = parts[2];

                Message storedMessage = messageIdMap.get(messageId);
                if (storedMessage != null && storedMessage.getUserName().equals(username)) {
                    // Update the message in repository
                    storedMessage.setContent(newContent);
                    msgRepo.save(storedMessage);

                    // Broadcast the edit
                    broadcast("__edited__ " + messageId + " " + username + ": " + newContent);
                    return;
                } else {
                    sendMessageToPArticularUser(username, "Cannot edit message: not found or not yours");
                    return;
                }
            }
        }

        // Handle deleting message
        if (message.startsWith("__delete__")) {
            // Format: __delete__ messageId
            String[] parts = message.split(" ", 2);
            if (parts.length == 2) {
                String messageId = parts[1];

                Message storedMessage = messageIdMap.get(messageId);
                if (storedMessage != null && storedMessage.getUserName().equals(username)) {
                    // Delete the message from repository
                    msgRepo.delete(storedMessage);
                    messageIdMap.remove(messageId);

                    // Broadcast the deletion
                    broadcast("__deleted__ " + messageId + " " + username);
                    return;
                } else {
                    sendMessageToPArticularUser(username, "Cannot delete message: not found or not yours");
                    return;
                }
            }
        }

        // Direct message to a user using the format "@username <message>"
        if (message.startsWith("@")) {
            String destUsername = message.split(" ")[0].substring(1);

            // send the message to the sender and receiver
            sendMessageToPArticularUser(destUsername, "[DM] " + username + ": " + message);
            sendMessageToPArticularUser(username, "[DM] " + username + ": " + message);

        }
        else { // broadcast
            broadcast(username + ": " + message);
        }

        // Saving chat history to repository
        msgRepo.save(new Message(username, message));
    }


    @OnClose
    public void onClose(Session session) throws IOException {
        logger.info("Entered into Close");

        // remove the user connection information
        String username = sessionUsernameMap.get(session);
        sessionUsernameMap.remove(session);
        usernameSessionMap.remove(username);

        // broadcase that the user disconnected
        String message = username + " disconnected [INACTIVE]";
        broadcast(message);
        userStatusMap.put(username, "inactive");
    }


    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
        logger.info("Entered into Error");
        throwable.printStackTrace();
    }


    private void sendMessageToPArticularUser(String username, String message) {
        try {
            usernameSessionMap.get(username).getBasicRemote().sendText(message);
        }
        catch (IOException e) {
            logger.info("Exception: " + e.getMessage().toString());
            e.printStackTrace();
        }
    }


    private void broadcast(String message) {
        sessionUsernameMap.forEach((session, username) -> {
            try {
                session.getBasicRemote().sendText(message);
            }
            catch (IOException e) {
                logger.info("Exception: " + e.getMessage().toString());
                e.printStackTrace();
            }

        });

    }


    // Gets the Chat history from the repository
    private String getChatHistory() {
        List<Message> messages = msgRepo.findAll();

        // convert the list to a string
        StringBuilder sb = new StringBuilder();
        if(messages != null && messages.size() != 0) {
            for (Message message : messages) {
                sb.append(message.getUserName() + ": " + message.getContent() + "\n");
            }
        }
        return sb.toString();
    }
}
