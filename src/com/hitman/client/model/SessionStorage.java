package com.hitman.client.model;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionStorage {

    public static final String PREFS_NAME = "HitmanPrefs";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String GCMID = "gcmid";
    public static final String GAME_ID = "gameId";
    public static final String TARGET = "target";

    private SharedPreferences prefs;

    public SessionStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public class NoCredentialsException extends StorageException {}

    public LoginCredentials readLoginCredentials() throws NoCredentialsException {
        String un = prefs.getString(USERNAME, null);
        String pw = prefs.getString(PASSWORD, null);
        String gcmId = prefs.getString(GCMID, null);
        if(un == null || pw == null || gcmId == null) {
            throw new NoCredentialsException();
        } else {
            return new LoginCredentials(gcmId, un, pw);
        }
    }

    public void saveLoginCredentials(LoginCredentials credentials) {
        prefs.edit().putString(USERNAME, credentials.getUsername())
                    .putString(PASSWORD, credentials.getPassword())
                    .putString(GCMID, credentials.getGcmId())
             .commit();
    }

    public String getCurrentTarget() {
        return prefs.getString(TARGET, null);
    }

    public void setCurrentTarget(String target) {
        prefs.edit().putString(TARGET, target).commit();
    }

    public void clearLoginCredentials() {
        prefs.edit().remove(USERNAME)
                    .remove(PASSWORD)
                    .remove(GAME_ID)
             .commit();
    }

}
