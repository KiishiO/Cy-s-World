package com.example.own_example.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Singleton service to handle user information, authentication and roles
 */
public class UserService {
    private static final String TAG = "UserService";
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FULL_NAME = "full_name";

    // User roles
    public static final String ROLE_STUDENT = "student";
    public static final String ROLE_TEACHER = "teacher";
    public static final String ROLE_ADMIN = "administrator";

    private static UserService instance;
    private SharedPreferences preferences;
    private String currentUserId;
    private String currentUsername;
    private String userRole;
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
        preferences = context.getApplicationContext()
                .getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);

        // Load user data
        try {
            long longUserId = preferences.getLong(KEY_USER_ID, 0);
            if (longUserId > 0) {
                currentUserId = String.valueOf(longUserId);
            } else {
                currentUserId = preferences.getString(KEY_USER_ID, null);
            }

            currentUsername = preferences.getString(KEY_USERNAME, null);
            userRole = preferences.getString(KEY_USER_ROLE, null);

            // Debug logging to see what's being loaded
            Log.d(TAG, "UserService initialized. User ID: " + currentUserId +
                    ", User: " + currentUsername +
                    ", Role: " + userRole);

            email = preferences.getString(KEY_EMAIL, null);
            fullName = preferences.getString(KEY_FULL_NAME, null);
        } catch (Exception e) {
            Log.e(TAG, "Error loading user data: " + e.getMessage());
        }

        isInitialized = true;
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

    public String getUserRole() {
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
        if (userRole == null) return false;

        // Check for string-based role
        if (ROLE_ADMIN.equals(userRole)) return true;

        // Check for enum-based role
        try {
            return "ADMIN".equals(userRole) || "ADMIN".equals(userRole.toUpperCase());
        } catch (Exception e) {
            Log.e(TAG, "Error checking admin role: " + e.getMessage());
            return false;
        }
    }

    public boolean isTeacher() {
        return ROLE_TEACHER.equals(userRole);
    }

    public boolean isStudent() {
        return ROLE_STUDENT.equals(userRole);
    }

    /**
     * Sets user data after successful login
     *
     * @param userId The user's unique identifier
     * @param username The user's username for display and communication
     * @param role The user's role (student, teacher, or administrator)
     * @param userEmail The user's email address
     * @param name The user's full name
     */
    public void setUserData(String userId, String username, String role, String userEmail, String name) {
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
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_USER_ROLE, role);
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
    public void setUserData(String userId, String username, String role) {
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