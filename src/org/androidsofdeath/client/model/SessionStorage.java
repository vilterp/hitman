package org.androidsofdeath.client.model;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionStorage {

    public static final String PREFS_NAME = "HitmanPrefs";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String GCMID = "gcmid";
    public static final String GAME_ID = "gameId";

    private SharedPreferences prefs;

    public SessionStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public LoginCredentials readLoginCredentials() {
        String un = prefs.getString(USERNAME, null);
        String pw = prefs.getString(PASSWORD, null);
        String gcmId = prefs.getString(GCMID, null);
        if(un == null || pw == null || gcmId == null) {
            return null;
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

    public int readGameId() {
        return prefs.getInt(GAME_ID, -1);
    }

    public void saveGameId(int id) {
        prefs.edit().putInt(GAME_ID, id).commit();
    }

    public void clearGameId() {
        prefs.edit().remove(GAME_ID).commit();
    }

}
