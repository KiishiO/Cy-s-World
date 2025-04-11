package com.example.own_example.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.own_example.UserRoles;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized service for authentication and authorization
 */
public class AuthService {
    private static final String TAG = "AuthService";

    // SharedPreferences keys
    public static final String PREFS_NAME_USER = "user_prefs";
    public static final String PREFS_NAME_LOGIN = "LoginPrefs";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_USER_ROLE = "user_role";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";

    // Authentication codes for restricted roles - must match backend exactly
    public static final String ADMIN_AUTH_CODE = "admin-secret-2025";
    public static final String TEACHER_AUTH_CODE = "teacher-access-2025";

    /**
     * Get the authentication code for the specified role
     */
    public static String getAuthCodeForRole(UserRoles role) {
        if (role == UserRoles.ADMIN) {
            return ADMIN_AUTH_CODE;
        } else if (role == UserRoles.TEACHER) {
            return TEACHER_AUTH_CODE;
        }
        return null; // Student role doesn't need auth code
    }

    /**
     * Get the authentication code for the current user
     */
    public static String getCurrentUserAuthCode(Context context) {
        UserRoles role = getUserRole(context);
        return getAuthCodeForRole(role);
    }

    /**
     * Get the current user's role
     */
    public static UserRoles getUserRole(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME_USER, Context.MODE_PRIVATE);
        String roleStr = prefs.getString(KEY_USER_ROLE, UserRoles.STUDENT.toString());

        try {
            return UserRoles.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid role string in preferences: " + roleStr);
            return UserRoles.STUDENT;
        }
    }

    /**
     * Check if the current user is an admin
     */
    public static boolean isAdmin(Context context) {
        return getUserRole(context) == UserRoles.ADMIN;
    }

    /**
     * Check if the current user is a teacher
     */
    public static boolean isTeacher(Context context) {
        return getUserRole(context) == UserRoles.TEACHER;
    }

    /**
     * Get the current user's ID
     */
    public static long getUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME_USER, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_USER_ID, -1);
    }

    /**
     * Append auth code as URL parameter based on role
     */
    public static String appendAuthCode(Context context, String baseUrl) {
        UserRoles role = getUserRole(context);
        String authCode = getAuthCodeForRole(role);

        if (authCode != null) {
            return baseUrl + (baseUrl.contains("?") ? "&" : "?") + "authCode=" + authCode;
        }
        return baseUrl;
    }

    /**
     * Get specific auth code URL for admin operations
     */
    public static String getAdminAuthUrl(String baseUrl) {
        return baseUrl + (baseUrl.contains("?") ? "&" : "?") + "authCode=" + ADMIN_AUTH_CODE;
    }

    /**
     * Get specific auth code URL for teacher operations
     */
    public static String getTeacherAuthUrl(String baseUrl) {
        return baseUrl + (baseUrl.contains("?") ? "&" : "?") + "authCode=" + TEACHER_AUTH_CODE;
    }
}