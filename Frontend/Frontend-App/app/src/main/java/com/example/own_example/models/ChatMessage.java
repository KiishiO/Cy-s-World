package com.example.own_example.models;

import java.util.Date;

/**
 * Model class for chat messages.
 */
public class ChatMessage {
    private Long id;
    private String userName;
    private String content;
    private Date sent;
    private boolean isDirectMessage;
    private String recipient;
    private boolean isMine;
    private String messageType; // "CHAT", "STATUS", "JOIN", "LEAVE"
    private boolean isEdited = false;
    private boolean isDeleted = false;
    private String originalContent;

    // Local identifier for tracking messages
    private String localId;

    public ChatMessage() {
        this.sent = new Date();
        this.localId = String.valueOf(System.currentTimeMillis());
    }

    public ChatMessage(String userName, String content) {
        this.userName = userName;
        this.content = content;
        this.sent = new Date();
        this.isDirectMessage = false;
        this.messageType = "CHAT";
        this.localId = String.valueOf(System.currentTimeMillis());
    }

    public ChatMessage(String userName, String content, String recipient) {
        this.userName = userName;
        this.content = content;
        this.sent = new Date();
        this.isDirectMessage = true;
        this.recipient = recipient;
        this.messageType = "CHAT";
        this.localId = String.valueOf(System.currentTimeMillis());
    }

    public static ChatMessage createStatusMessage(String content) {
        ChatMessage message = new ChatMessage();
        message.setContent(content);
        message.setMessageType("STATUS");
        return message;
    }

    public static ChatMessage createEditedMessage(ChatMessage original, String newContent) {
        ChatMessage message = new ChatMessage();
        message.setId(original.getId());
        message.setLocalId(original.getLocalId());
        message.setUserName(original.getUserName());
        message.setContent(newContent);
        message.setOriginalContent(original.getContent());
        message.setSent(new Date());
        message.setDirectMessage(original.isDirectMessage());
        message.setRecipient(original.getRecipient());
        message.setMine(original.isMine());
        message.setMessageType("CHAT");
        message.setEdited(true);
        return message;
    }

    public static ChatMessage createDeletedMessage(ChatMessage original) {
        ChatMessage message = new ChatMessage();
        message.setId(original.getId());
        message.setLocalId(original.getLocalId());
        message.setUserName(original.getUserName());
        message.setContent("This message has been deleted");
        message.setOriginalContent(original.getContent());
        message.setSent(original.getSent());
        message.setDirectMessage(original.isDirectMessage());
        message.setRecipient(original.getRecipient());
        message.setMine(original.isMine());
        message.setMessageType("CHAT");
        message.setDeleted(true);
        return message;
    }

    public static ChatMessage createFromWebSocketMessage(String message, String currentUser) {
        ChatMessage chatMessage = new ChatMessage();

        // Handle different message types
        if (message.startsWith("User:") && message.contains("has Joined the Chat")) {
            // Join message
            String username = message.substring(5, message.indexOf(" has Joined"));
            chatMessage.setUserName(username);
            chatMessage.setContent(message);
            chatMessage.setMessageType("JOIN");
        } else if (message.contains("disconnected")) {
            // Leave message
            String username = message.substring(0, message.indexOf(" disconnected"));
            chatMessage.setUserName(username);
            chatMessage.setContent(message);
            chatMessage.setMessageType("LEAVE");
        } else if (message.contains(" is now ")) {
            // Status change message
            String[] parts = message.split(" is now ");
            chatMessage.setUserName(parts[0]);
            chatMessage.setContent(message);
            chatMessage.setMessageType("STATUS");
        } else if (message.startsWith("[DM]")) {
            // Direct message
            int firstColon = message.indexOf(":");
            String sender = message.substring(5, firstColon).trim();
            String content = message.substring(firstColon + 1).trim();

            chatMessage.setUserName(sender);
            chatMessage.setContent(content);
            chatMessage.setDirectMessage(true);
            chatMessage.setMessageType("CHAT");

            // Check if this message is from the current user
            if (sender.equals(currentUser)) {
                chatMessage.setMine(true);
            }

            // Extract recipient from message format @username
            if (content.startsWith("@")) {
                int spaceIndex = content.indexOf(" ");
                if (spaceIndex > 0) {
                    String recipient = content.substring(1, spaceIndex);
                    chatMessage.setRecipient(recipient);
                }
            }
        } else {
            // Regular chat message
            int colonIndex = message.indexOf(":");
            if (colonIndex > 0) {
                String sender = message.substring(0, colonIndex).trim();
                String content = message.substring(colonIndex + 1).trim();

                chatMessage.setUserName(sender);
                chatMessage.setContent(content);
                chatMessage.setMessageType("CHAT");

                // Check if this message is from the current user
                if (sender.equals(currentUser)) {
                    chatMessage.setMine(true);
                }
            } else {
                // Fallback for messages without a clear format
                chatMessage.setContent(message);
                chatMessage.setMessageType("STATUS");
            }
        }

        return chatMessage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getSent() {
        return sent;
    }

    public void setSent(Date sent) {
        this.sent = sent;
    }

    public boolean isDirectMessage() {
        return isDirectMessage;
    }

    public void setDirectMessage(boolean directMessage) {
        isDirectMessage = directMessage;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public void setEdited(boolean edited) {
        isEdited = edited;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String getOriginalContent() {
        return originalContent;
    }

    public void setOriginalContent(String originalContent) {
        this.originalContent = originalContent;
    }
}