package com.example.own_example.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.own_example.services.AuthService;
import com.example.own_example.UserRoles;

/**
 * Singleton service to handle user information, authentication and roles
 */
public class UserService {
    private static final String TAG = "UserService";
    private static final String PREF_NAME = "user_preferences";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FULL_NAME = "full_name";

    private static UserService instance;
    private SharedPreferences preferences;
    private String currentUserId;
    private String currentUsername;
    private UserRoles userRole;
    private String email;
    private String fullName;
    private boolean isInitialized = false;

    private UserService() {
        // Private constructor to enforce singleton pattern
    }

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    public void initialize(Context context) {
        if (context == null) {
            Log.e(TAG, "Cannot initialize UserService with null context");
            return;
        }

        // Try to load from both preference stores
        // First try "LoginPrefs"
        preferences = context.getApplicationContext()
                .getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);

        // Load user data with proper type handling
        try {
            // Try to get user_id as a long first (matching how it's stored)
            long longUserId = preferences.getLong(KEY_USER_ID, 0);
            if (longUserId > 0) {
                currentUserId = String.valueOf(longUserId);
            } else {
                // Fall back to string if needed
                currentUserId = preferences.getString(KEY_USER_ID, null);
            }
        } catch (ClassCastException e) {
            // If there's a type mismatch, try the other way
            try {
                currentUserId = preferences.getString(KEY_USER_ID, null);
            } catch (Exception ex) {
                Log.e(TAG, "Error loading user_id: " + ex.getMessage());
                currentUserId = null;
            }
        }

        // Load other user data
        try {
            currentUsername = preferences.getString(KEY_USERNAME, null);
            String roleString = preferences.getString(KEY_USER_ROLE, null);
            email = preferences.getString(KEY_EMAIL, null);
            fullName = preferences.getString(KEY_FULL_NAME, null);

            // Convert string role to enum
            if (roleString != null) {
                try {
                    userRole = UserRoles.valueOf(roleString);
                } catch (IllegalArgumentException ex) {
                    Log.e(TAG, "Invalid role format: " + roleString);
                    userRole = UserRoles.STUDENT; // Default to STUDENT
                }
            } else {
                userRole = UserRoles.STUDENT; // Default role
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading user data: " + e.getMessage());
        }

        isInitialized = true;

        Log.d(TAG, "UserService initialized. User ID: " + currentUserId +
                ", User: " + (currentUsername != null ? currentUsername : "not logged in") +
                ", Role: " + (userRole != null ? userRole.toString() : "none"));
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public UserRoles getUserRole() {
        return userRole;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isLoggedIn() {
        return currentUserId != null && currentUsername != null;
    }

    public boolean isAdmin() {
        return userRole == UserRoles.ADMIN;
    }

    public boolean isTeacher() {
        return userRole == UserRoles.TEACHER;
    }

    public boolean isStudent() {
        return userRole == UserRoles.STUDENT;
    }

    /**
     * Get authentication code for current user role
     */
    public String getAuthCode() {
        if (userRole == UserRoles.ADMIN) {
            return AuthService.ADMIN_AUTH_CODE;
        } else if (userRole == UserRoles.TEACHER) {
            return AuthService.TEACHER_AUTH_CODE;
        }
        return null;
    }

    /**
     * Append auth code to URL if needed for current user role
     */
    public String appendAuthCode(String baseUrl) {
        String authCode = getAuthCode();
        if (authCode != null) {
            return baseUrl + (baseUrl.contains("?") ? "&" : "?") + "authCode=" + authCode;
        }
        return baseUrl;
    }

    /**
     * Sets user data after successful login
     *
     * @param userId The user's unique identifier
     * @param username The user's username for display and communication
     * @param role The user's role (STUDENT, TEACHER, or ADMIN)
     * @param userEmail The user's email address
     * @param name The user's full name
     */
    public void setUserData(String userId, String username, UserRoles role, String userEmail, String name) {
        if (!isInitialized) {
            Log.e(TAG, "Cannot set user data: UserService not initialized");
            return;
        }

        this.currentUserId = userId;
        this.currentUsername = username;
        this.userRole = role;
        this.email = userEmail;
        this.fullName = name;

        SharedPreferences.Editor editor = preferences.edit();

        // Try to store as long if possible
        try {
            editor.putLong(KEY_USER_ID, Long.parseLong(userId));
        } catch (NumberFormatException e) {
            editor.putString(KEY_USER_ID, userId);
        }

        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_USER_ROLE, role.toString());
        editor.putString(KEY_EMAIL, userEmail);
        editor.putString(KEY_FULL_NAME, name);
        editor.apply();

        Log.d(TAG, "User data saved: " + username + ", role: " + role);
    }

    /**
     * Simplified login with just the basic required fields
     *
     * @param userId The user's unique identifier
     * @param username The user's username
     * @param role The user's role
     */
    public void setUserData(String userId, String username, UserRoles role) {
        setUserData(userId, username, role, null, null);
    }

    /**
     * Logs out the current user and clears all user data
     */
    public void logout() {
        if (!isInitialized) {
            Log.e(TAG, "Cannot logout: UserService not initialized");
            return;
        }

        // Clear memory data
        currentUserId = null;
        currentUsername = null;
        userRole = null;
        email = null;
        fullName = null;

        // Clear persistent data
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_USER_ROLE);
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_FULL_NAME);
        editor.apply();

        Log.d(TAG, "User logged out and data cleared");
    }

    /**
     * Checks if the current user has a specific permission
     *
     * @param permission The permission to check
     * @return true if the user has the permission, false otherwise
     */
    public boolean hasPermission(String permission) {
        // This is where you would implement more granular permission checks
        // For now, we'll just use role-based permissions

        if (!isLoggedIn()) {
            return false;
        }

        // Administrators have all permissions
        if (isAdmin()) {
            return true;
        }

        // Example permission checks for other roles
        switch (permission) {
            case "view_events":
                // All logged-in users can view events
                return true;

            case "rsvp_events":
                // Students and teachers can RSVP to events
                return isStudent() || isTeacher();

            case "manage_events":
                // Only admins can manage events
                return false;

            default:
                return false;
        }
    }
}