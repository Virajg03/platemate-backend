package com.example.platemate;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_ROLE = "user_role";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PROFILE_COMPLETE = "profile_complete";
    private static final String KEY_USER_ID = "user_id";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Save login state with all details
    public void saveLoginSession(String token, String refreshToken, String role, String username, Long userId) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_USERNAME, username);
        if (userId != null) {
            editor.putLong(KEY_USER_ID, userId);
        }
        editor.apply();
    }

    // Legacy method for backward compatibility
    public void saveLoginSession(String token) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    // Update tokens after refresh
    public void updateTokens(String token, String refreshToken) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.apply();
    }

    // Mark profile as complete
    public void setProfileComplete(boolean isComplete) {
        editor.putBoolean(KEY_PROFILE_COMPLETE, isComplete);
        editor.apply();
    }

    // Get stored token
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    // Get refresh token
    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    // Get user role
    public String getRole() {
        return prefs.getString(KEY_ROLE, null);
    }

    // Get username
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    // Get user ID
    public Long getUserId() {
        long userId = prefs.getLong(KEY_USER_ID, -1);
        return userId == -1 ? null : userId;
    }

    // Check if profile is complete
    public boolean isProfileComplete() {
        return prefs.getBoolean(KEY_PROFILE_COMPLETE, false);
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Clear session
    public void logout() {
        editor.clear();
        editor.apply();
    }
}
