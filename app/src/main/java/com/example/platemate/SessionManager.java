package com.example.platemate;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;

/**
 * Simple session manager backed by SharedPreferences.
 * Designed to be defensive against null values (avoids NPEs in callers).
 */
public class SessionManager {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_ROLE = "user_role";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PROFILE_COMPLETE = "profile_complete";
    private static final String KEY_USER_ID = "user_id";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        // Use application context to avoid leaking an Activity context
        Context appCtx = context.getApplicationContext();
        prefs = appCtx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Save login session with full details.
     */
    public void saveLoginSession(String token,
                                 @Nullable String refreshToken,
                                 @Nullable String role,
                                 @Nullable String username,
                                 @Nullable Long userId) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_TOKEN, token);
        if (refreshToken != null) {
            editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        } else {
            editor.remove(KEY_REFRESH_TOKEN);
        }
        // Store a non-null role to make client code safer (avoids switch() NPE)
        editor.putString(KEY_ROLE, role != null ? role : "");
        editor.putString(KEY_USERNAME, username != null ? username : "");
        if (userId != null) {
            editor.putLong(KEY_USER_ID, userId);
        } else {
            editor.remove(KEY_USER_ID);
        }
        editor.apply();
    }

    /**
     * Legacy save method kept for backward compatibility.
     * Writes safe defaults to avoid leaving an inconsistent session.
     */
    public void saveLoginSession(String token) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_TOKEN, token);
        // set safe defaults to avoid null role/username when code expects them
        editor.putString(KEY_ROLE, "");
        editor.putString(KEY_USERNAME, "");
        editor.remove(KEY_USER_ID);
        editor.apply();
    }

    /**
     * Update tokens after refresh.
     */
    public void updateTokens(String token, @Nullable String refreshToken) {
        if (token != null) editor.putString(KEY_TOKEN, token);
        if (refreshToken != null) editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.apply();
    }

    /**
     * Mark profile as complete / incomplete.
     */
    public void setProfileComplete(boolean isComplete) {
        editor.putBoolean(KEY_PROFILE_COMPLETE, isComplete);
        editor.commit(); // Use commit() instead of apply() for immediate write
    }
    
    // Verify profile complete status was saved
    public boolean verifyProfileComplete() {
        boolean saved = prefs.getBoolean(KEY_PROFILE_COMPLETE, false);
        if (!saved) {
            // Retry saving
            editor.putBoolean(KEY_PROFILE_COMPLETE, true);
            editor.commit();
            return prefs.getBoolean(KEY_PROFILE_COMPLETE, false);
        }
        return true;
    }

    /** Get stored auth token (may be null). */
    @Nullable
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    /** Get refresh token (may be null). */
    @Nullable
    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    /**
     * Get user role. Returns empty string if not set (avoids NPE if used in switch()).
     * If you prefer to detect "not set", change the default to null.
     */
    public String getRole() {
        return prefs.getString(KEY_ROLE, "");
    }

    /** Get username (empty string if not set). */
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    /**
     * Get user id. Returns null if not present.
     */
    @Nullable
    public Long getUserId() {
        if (!prefs.contains(KEY_USER_ID)) return null;
        long id = prefs.getLong(KEY_USER_ID, -1L);
        return id == -1L ? null : id;
    }

    /** Check if profile is complete. */
    public boolean isProfileComplete() {
        return prefs.getBoolean(KEY_PROFILE_COMPLETE, false);
    }

    /** Check if user is logged in. */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Clears all session data.
     * Alias clearSession kept for readability.
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }

    public void clearSession() {
        logout();
    }

    // Optional: helper setters/removers for individual fields
    public void setRole(@Nullable String role) {
        editor.putString(KEY_ROLE, role != null ? role : "");
        editor.apply();
    }

    public void removeRole() {
        editor.remove(KEY_ROLE);
        editor.apply();
    }
}
